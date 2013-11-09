import java.io.*;

public class BlackBox {

    private static final PrintStream out = System.out;
    private static final InputStream in = System.in;

    private int testsFailed, testsPassed;
    private BufferedReader reader;
    private PrintWriter writer;

    public void run() throws IOException {
        PipedInputStream programOut = new PipedInputStream();
        reader = new BufferedReader(new InputStreamReader(programOut));
        System.setOut(new PrintStream(new PipedOutputStream(programOut), true));

        PipedOutputStream programIn = new PipedOutputStream();
        writer = new PrintWriter(programIn, true);
        System.setIn(new BufferedInputStream(new PipedInputStream(programIn)));

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    MasterMind.main(new String[0]);
                } catch(Exception e) {
                    System.err.println("The game crashed:");
                    e.printStackTrace();
                }
            }
        };

        thread.start();

        testsFailed = 0;
        testsPassed = 0;

        try {
            doTests();
            System.err.println();
            System.err.printf("Tests failed: %d; tests succeeded: %d.%n", testsFailed, testsPassed);
        } catch(Exception e) {
            System.err.println("Fatal error:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String[] readLines(long timeout) {
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

    private String[] readLines() {
        return readLines(1000);
    }

    private void printOutput() {
        printOutput(readLines());
    }

    private static void printOutput(String[] lines) {
        if (lines.length > 0) {
            out.println("OUT:   " + lines[0]);
            for (int i = 1; i < lines.length; i++) {
                out.println("       " + lines[i]);
            }
        }
    }

    private void performInput(char input) {
        writer.println(input);
        out.println("IN:    " + input);
    }

    private void test(boolean expression) {
        if (expression) {
            testsPassed++;
        } else {
            testsFailed++;
            System.err.printf("Test %d failed.%n", testsPassed + testsFailed);
        }
    }

    public void doTests() throws IOException {
        // Check welcome screen
        String[] lines = readLines();
        printOutput(lines);
        test(lines.length > 0);
        test(lines[0].startsWith("Welcome to MasterMind"));
        test(lines[lines.length - 1].startsWith("Ready to start"));

        // Enter yes to play a game
        performInput('n');
        printOutput();

//        performInput('s');
//        printOutput();
//
//        performInput('r');
//        printOutput();
    }

    public static void main(String[] args) throws IOException {
        BlackBox blackBox = new BlackBox();
        blackBox.run();
    }

}
