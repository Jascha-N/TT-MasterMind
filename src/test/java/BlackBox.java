import java.io.*;

public abstract class BlackBox {

    private static final long    TIMEOUT     = Long.parseLong(      System.getProperty("blackbox.timeout", "100" ));
    private static final int     BUFFER_SIZE = Integer.parseInt(    System.getProperty("blackbox.buffer" , "8129"));
    private static final boolean PRINT_DEBUG = Boolean.parseBoolean(System.getProperty("blackbox.debug"  , "true"));

    private int testsFailed, testsPassed;
    private Reader reader;
    private PrintWriter writer;
    private volatile Throwable programException;
    private volatile boolean programTerminated;

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
        PipedInputStream programOut = new PipedInputStream(BUFFER_SIZE);
        reader = new InputStreamReader(programOut);
        System.setOut(new PrintStream(new PipedOutputStream(programOut), true));

        in = System.in;
        PipedOutputStream programIn = new PipedOutputStream();
        writer = new PrintWriter(programIn, true);
        System.setIn(new PipedInputStream(programIn));

        testsFailed = 0;
        testsPassed = 0;

        programTerminated = false;
        programException = null;

        try {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        runProgram(args);
                    } catch(Throwable t) {
                        programException = t;
                    }
                    programTerminated = true;
                }
            };

            thread.start();

            try {
                performTests();
//                try {
//                    thread.join(); // Wait for program to finish
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
                System.err.println();
                System.err.printf("Tests failed: %d; tests succeeded: %d.%n", testsFailed, testsPassed);
            } catch(ProgramTerminatedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } finally {
            System.in.close();
            System.out.close();
            reader.close();
            writer.close();
            System.setOut(out);
            System.setIn(in);
        }
    }

    /**
     * Checks if the program has terminated and throws an exception if it has.
     *
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    protected void checkCrash() throws ProgramTerminatedException {
        if (programException != null) {
            throw new ProgramTerminatedException("The program crashed: " + programException, programException);
        }
        if (programTerminated) {
            throw new ProgramTerminatedException("Program terminated unexpectedly.");
        }
    }

    /**
     * Reads all lines from the program's output.
     *
     * It will stop reading data if no new data has been received for the configured timeout duration.
     *
     * @return output lines
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    protected String[] readLines() throws ProgramTerminatedException {
        StringBuilder builder = new StringBuilder();
        try {
            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() < start + TIMEOUT || reader.ready()) {
                checkCrash();

                if (reader.ready()) {
                    int c = reader.read();
                    if (c == -1) // End of stream; generally should not happen
                        break;
                    builder.append((char) c);

//                    if (builder.length() % BUFFER_SIZE == 0) { // Wait for buffer to refill
//                        try {
//                            Thread.sleep(10 * TIMEOUT);
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }

                    start = System.currentTimeMillis();
                } else {
                    Thread.yield();
                }
            }
        } catch (IOException e) {
            checkCrash(); // If the pipe end has closed
        }

        if (builder.length() > 0)
            return builder.toString().split("\r?\n");

        return new String[0];
    }

    /**
     * Reads all output lines, prints and then returns the result.
     *
     * @return output lines
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    protected String[] readAndPrintLines() throws ProgramTerminatedException {
        String[] output = readLines();
        printOutput(output);
        return output;
    }

    /**
     * Prints the given lines to (the actual) stdout.
     *
     * @param lines lines to print
     */
    protected void printOutput(String[] lines) {
        if (PRINT_DEBUG) {
            if (lines.length > 0) {
                out.println("OUT:   " + lines[0]);
                for (int i = 1; i < lines.length; i++) {
                    out.println("       " + lines[i]);
                }
            }
        }
    }

    /**
     * Presents the program with the given line of input.
     *
     * @param line line of input
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    protected void performInput(String line) throws ProgramTerminatedException {
        checkCrash();
        writer.println(line);
        if (PRINT_DEBUG)
            out.println("IN:    " + line);
    }

    /**
     * Presents the program with the given character as a line of input.
     *
     * @param input input character
     * @throws ProgramTerminatedException when the tested program has terminated
     *
     * @see BlackBox#performInput(String)
     */
    protected void performInput(char input) throws ProgramTerminatedException {
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
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    protected abstract void performTests() throws ProgramTerminatedException;

}
