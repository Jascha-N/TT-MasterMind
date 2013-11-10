import java.io.*;

public abstract class BlackBox {

    private static final long DEFAULT_READ_DURATION = 100;

    private int testsFailed, testsPassed;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * Access to the actual stdout.
     */
    protected PrintStream out;

    /**
     * Access to the actual stdin.
     */
    protected InputStream in;

    /**
     * Sets up the system, runs the program and performs the tests.
     *
     * @param args program arguments
     * @throws IOException
     */
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

    /**
     * Reads all lines from the program's output within a given amount of time.
     *
     * It will keep reading data if it is available even when the duration has passed.
     *
     * @param duration capture duration in milliseconds
     * @return output lines
     */
    protected String[] readLines(long duration) {
        long start = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        try {
            while (System.currentTimeMillis() < start + duration || reader.ready()) {
                if (reader.ready()) {
                    int c = reader.read();
                    if (c == -1) // End of stream; generally should not happen
                        break;
                    builder.append((char) c);
                } else {
                    Thread.yield();
                }
            }
        } catch (IOException e) {
            // Nothing to do here
        }
        if (builder.length() > 0)
            return builder.toString().split("\r?\n");

        return new String[0];
    }

    /**
     * Read output with the default duration.
     *
     * @return output lines
     *
     * @see BlackBox#readLines(long)
     */
    protected String[] readLines() {
        return readLines(DEFAULT_READ_DURATION);
    }

    /**
     * Reads all output lines for the given duration, prints and then returns the result.
     *
     * @param duration capture duration in milliseconds
     * @return output lines
     *
     * @see BlackBox#readLines(long)
     */
    protected String[] readAndPrintLines(long duration) {
        String[] output = readLines(duration);
        printOutput(output);
        return output;
    }

    /**
     * Reads all output lines for the default duration, prints and then returns the result.
     *
     * @return output lines
     *
     * @see BlackBox#readLines(long)
     * @see BlackBox#readAndPrintLines(long)
     */
    protected String[] readAndPrintLines() {
        return readAndPrintLines(DEFAULT_READ_DURATION);
    }

    /**
     * Prints the given lines to (the actual) stdout.
     *
     * @param lines lines to print
     */
    protected void printOutput(String[] lines) {
        if (lines.length > 0) {
            out.println("OUT:   " + lines[0]);
            for (int i = 1; i < lines.length; i++) {
                out.println("       " + lines[i]);
            }
        }
    }

    /**
     * Presents the program with the given line of input.
     *
     * @param line line of input
     */
    protected void performInput(String line) {
        writer.println(line);
        out.println("IN:    " + line);
    }

    /**
     * Presents the program with the given character as a line of input.
     *
     * @param input input character
     *
     * @see BlackBox#performInput(String)
     */
    protected void performInput(char input) {
        performInput(String.valueOf(input));
    }

    /**
     * Tests if the given predicate holds.
     *
     * If it does not hold, it will show a message containing the given description for this test.
     *
     * @param description test description
     * @param expression predicate to check
     */
    protected void test(String description, boolean expression) {
        if (expression) {
            testsPassed++;
        } else {
            testsFailed++;
            System.err.printf("TEST:  %s failed.%n", description);
        }
    }

    /**
     * Tests if the given predicate holds.
     *
     * @param expression predicate to check
     *
     * @see BlackBox#test(String, boolean)
     */
    protected void test(boolean expression) {
        test("Test", expression);
    }

    /**
     * Runs the program to be tested.
     *
     * @param args program arguments
     * @throws Exception
     */
    protected abstract void runProgram(String args[]) throws Exception;

    /**
     * Performs a series of user-defined tests.
     *
     * @throws Exception
     */
    protected abstract void performTests() throws Exception;

}
