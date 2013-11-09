import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BlackBox {

    private static final PrintStream out = System.out;
    private static final InputStream in = System.in;

    public void run() throws IOException {
        PipedInputStream programOut = new PipedInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(programOut));
        System.setOut(new PrintStream(new PipedOutputStream(programOut), true));

        PipedOutputStream programIn = new PipedOutputStream();
        PrintWriter writer = new PrintWriter(programIn, true);
        System.setIn(new PipedInputStream(programIn));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MasterMind.main(new String[0]);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();

        doTests(reader, writer);
    }

    private static String[] readAllLines(final BufferedReader reader, long timeout) {
        final List<String> lines = new ArrayList<>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        String line = reader.readLine();
                        if (line == null)
                            break;
                        lines.add(line);
                    } catch (InterruptedIOException e) {
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        // stop
                    }
                }
            }
        });
        thread.start();
        try {
            thread.join(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return lines.toArray(new String[lines.size()]);
    }

    private static String[] readAllLines(final BufferedReader reader) {
        return readAllLines(reader, 1000);
    }

    public void doTests(BufferedReader reader, PrintWriter writer) throws IOException {
//        String[] lines = readAllLines(reader);
//        for (String line : lines) {
//            System.err.println(line);
//        }
//        writer.println("n");

        // CODE GOES HERE!
    }

    public static void main(String[] args) throws IOException {
        BlackBox blackBox = new BlackBox();
        blackBox.run();
    }

}
