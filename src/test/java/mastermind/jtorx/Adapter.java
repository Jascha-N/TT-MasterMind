package mastermind.jtorx;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class Adapter {

    private volatile Thread thread;
    private PrintWriter writer;
    private Process process;

    private final BlockingQueue<String> outputEvents = new LinkedBlockingQueue<>();
    private final long timeout;

    public Adapter(long timeout) {
        this.timeout = timeout;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String result;
            do {
                String line = reader.readLine();
                if (line == null)
                    break;

                result = parseCommandLine(line);
                System.out.println(result + " "); // Workaround for a JTorX bug
            } while (!result.equals("A_QUIT"));

            if (thread != null) {
                System.out.println("kaas");
                thread.interrupt();
                process.destroy();
            }
        } catch (IOException e) {
            System.err.println("I/O exception: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String parseCommandLine(String line) {
        String[] commandAndArgs = line.split("[\t ]+", 2);

        String command = commandAndArgs[0];
        String[] unparsedArgs = commandAndArgs.length > 1 ? commandAndArgs[1].split("\t") : new String[0];

        Map<String, String> arguments = new HashMap<>();
        for (String unparsedArg : unparsedArgs) {
            if (unparsedArg.indexOf('=') == -1)
                return "A_ERROR UnknownCommand Unable to parse arguments";

            String[] argParts = unparsedArg.split("=", 2);
            if (argParts.length < 2)
                return "A_ERROR UnknownCommand Unable to parse arguments";

            arguments.put(argParts[0], argParts[1]);
        }

        try {
            switch (command) {
                case "C_IOKIND":
                {
                    String kind = arguments.get("iokind");

                    StringBuilder builder = new StringBuilder();
                    builder.append("A_IOKIND");
                    if (kind == null)
                        builder.append(" iokind=").append(outputEvents.isEmpty() ? "input" : "output");
                    else if (kind.equals("input") || kind.equals("output"))
                        builder.append(" iokind=").append(kind);
                    else
                        return "A_ERROR UnknownIOKind " + kind;

                    if (arguments.containsKey("channel"))
                        builder.append("\tchannel=").append(arguments.get("channel"));

                    return builder.toString();
                }
                case "C_INPUT":
                {
                    String event = arguments.get("event");
                    if (event == null)
                        return "A_ERROR MissingArgument event";

                    String error = performInput(event);
                    if (error != null)
                        return error;

                    StringBuilder builder = new StringBuilder();
                    builder.append("A_INPUT event=").append(event);
                    if (arguments.containsKey("channel"))
                        builder.append("\tchannel=").append(arguments.get("channel"));

                    return builder.toString();
                }
                case "C_OUTPUT":
                {
                    String event = outputEvents.poll(timeout, TimeUnit.MILLISECONDS);

                    StringBuilder builder = new StringBuilder();
                    builder.append("A_OUTPUT ").append(event == null ? "suspension=1" : "event=" + event);
                    if (arguments.containsKey("channel"))
                        builder.append("\tchannel=").append(arguments.get("channel"));

                    return builder.toString();
                }
                case "C_QUIT":
                    return "A_QUIT";
                default:
                    return "A_ERROR UnknownCommand Unknown command: " + command;
            }
        } catch (Exception e) {
            System.out.println("A_ERROR InternalError " + e);
            throw new RuntimeException(e);
        }
    }

    private void startSUT() throws IOException {
        assert thread == null;

        ProcessBuilder builder = new ProcessBuilder("java");
        for (String arg : getJavaArguments())
            builder.command().add(arg);

        process = builder.start();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new PrintWriter(process.getOutputStream(), true);
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    try {
                        StringBuilder builder = new StringBuilder();
                        while (!Thread.interrupted()) {
                            long start = System.currentTimeMillis();
                            int c = 0;
                            do {
                                if (reader.ready()) {
                                    c = reader.read();
                                    if (c == -1)
                                        break;
                                    if (c != '\r')
                                        builder.append((char) c);
                                } else
                                    Thread.yield();
                            } while (!Thread.interrupted() && (System.currentTimeMillis() < start + timeout / 10L || builder.length() == 0) && c != '\n');

                            classifyOutput(builder);

                            if (c == -1)
                                break;
                        }
                    } catch (IOException ignored) {

                    }

                    try {
                        outputEvents.add(process.exitValue() == 0 ? "!Stopped" : "!Crashed");
                    } catch (IllegalThreadStateException ignored) {
                        process.destroy();
                        outputEvents.add("!Terminated");
                    }
                } finally {
                    thread = null;
                }
            }
        };
        thread.start();
        outputEvents.add("!Started");
    }



    private String performInput(String inputLabel) throws IOException {
        if (inputLabel.equals("?Start")) {
            if (thread != null)
                return "A_INPUT_ERROR";
            startSUT();
        } else {
            String input = getInputFromLabel(inputLabel);
            if (input == null)
                return "A_ERROR ParseErrorEvent Unknown event: " + inputLabel;

            if (thread == null)
                return "A_INPUT_ERROR";

            writer.println(input);
        }

        return null;
    }

    private void classifyOutput(StringBuilder rawOutput) {
        for (OutputClassifier c : getOutputClassifiers()) {
            int length = c.run(rawOutput);
            if (length >= 0) {
                System.err.println(rawOutput.subSequence(0, length));
                rawOutput.delete(0, length);
                outputEvents.add(c.getLabel());
                break;
            }
        }
    }

    protected abstract String getInputFromLabel(String label);
    protected abstract OutputClassifier[] getOutputClassifiers();
    protected abstract String[] getJavaArguments();

}
