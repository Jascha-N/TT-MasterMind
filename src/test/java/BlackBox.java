import java.io.*;

public abstract class BlackBox {

    protected PrintStream out;
    protected InputStream in;
    private int testsFailed, testsPassed;
    private BufferedReader reader;
    private PrintWriter writer;

    public void run(final String[] args) throws IOException {
        out = System.out;
        PipedInputStream programOut = new PipedInputStream();
        reader = new BufferedReader(new InputStreamReader(programOut));
        System.setOut(new PrintStream(new PipedOutputStream(programOut), true));

        in = System.in;
        PipedOutputStream programIn = new PipedOutputStream();
        writer = new PrintWriter(programIn, true);
        System.setIn(new BufferedInputStream(new PipedInputStream(programIn)));

        try {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        runProgram(args);
                    } catch(Exception e) {
                        System.err.println("The tested program crashed:");
                        e.printStackTrace();
                    }
                }
            };

            thread.start();

            testsFailed = 0;
            testsPassed = 0;

            try {
                performTests();
                System.err.println();
                System.err.printf("Tests failed: %d; tests succeeded: %d.%n", testsFailed, testsPassed);
            } catch(Exception e) {
                System.err.println("Fatal error:");
                e.printStackTrace();
                System.exit(1);
            }
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

    protected String[] readLines(long timeout) {
        long start = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        try {
            while (System.currentTimeMillis() < start + timeout) {
                if (reader.ready()) {
                    int c = reader.read();
                    if (c == -1) // End of stream; generally should not happen
                        break;
                    builder.append((char) c);
                }
            }
        } catch (IOException e) {
            // Nothing to do here
        }
        if (builder.length() > 0)
            return builder.toString().split("\r?\n");

        return new String[0];
    }

    protected String[] readLines() {
        return readLines(1000);
    }

    protected void printOutput() {
        printOutput(readLines());
    }

    protected void printOutput(String[] lines) {
        if (lines.length > 0) {
            out.println("OUT:   " + lines[0]);
            for (int i = 1; i < lines.length; i++) {
                out.println("       " + lines[i]);
            }
        }
    }

    protected void performInput(char input) {
        performInput(String.valueOf(input));
    }

    protected void performInput(String input) {
        writer.println(input);
        out.println("IN:    " + input);
    }

    protected void test(boolean expression) {
        if (expression) {
            testsPassed++;
        } else {
            testsFailed++;
            System.err.printf("TEST:  Test %d failed.%n", testsPassed + testsFailed);
        }
    }

    protected abstract void runProgram(String args[]) throws Exception;

    protected abstract void performTests() throws Exception;

}
