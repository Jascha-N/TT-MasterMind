import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import org.apache.log4j.Logger;
import org.graphwalker.Util;
import org.graphwalker.conditions.EdgeCoverage;
import org.graphwalker.exceptions.InvalidDataException;
import org.graphwalker.exceptions.StopConditionException;
import org.graphwalker.generators.A_StarPathGenerator;
import org.graphwalker.generators.PathGenerator;
import org.graphwalker.multipleModels.ModelHandler;

public class MasterMindModel extends org.graphwalker.multipleModels.ModelAPI {

    private static final long    TIMEOUT     = Long.parseLong(      System.getProperty("blackbox.timeout", "100" ));
    private static final int     BUFFER_SIZE = Integer.parseInt(    System.getProperty("blackbox.buffer" , "8129"));
    private static final boolean PRINT_DEBUG = Boolean.parseBoolean(System.getProperty("blackbox.debug"  , "true"));

    private int testsFailed, testsPassed;
    private Reader reader;
    private PrintWriter writer;
    private volatile Throwable programException;
    private volatile boolean programTerminated;

    private static Logger logger = Util.setupLogger(MasterMindModel.class);
    private static final Random random = new Random();
    private static final char[] PEGS = {'r', 'o', 'y', 'g', 'b', 'i', 'v'};

    /**
     * Access to the actual stdout.
     */
    private PrintStream out;

    /**
     * Access to the actual stdin.
     */
    private InputStream in;

    public MasterMindModel(File model, PathGenerator generator) {
        super(model, true, generator, false);
    }

    /**
     * Sets up the system, runs the program and performs the tests.
     *
     * @param args program arguments
     * @throws IOException
     */
    private void run(final String[] args) throws IOException, ProgramTerminatedException {
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

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    MasterMind.main(args);
                } catch(Throwable t) {
                    programException = t;
                }
                programTerminated = true;
            }
        };

        thread.start();
    }

    /**
     * Checks if the program has terminated and throws an exception if it has.
     *
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    private void checkCrash() throws ProgramTerminatedException {
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
    private String[] readLines() throws ProgramTerminatedException {
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
    private String[] readAndPrintLines() throws ProgramTerminatedException {
        String[] output = readLines();
        printOutput(output);
        return output;
    }

    /**
     * Prints the given lines to (the actual) stdout.
     *
     * @param lines lines to print
     */
    private void printOutput(String[] lines) {
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
    private void performInput(String line) throws ProgramTerminatedException {
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
    private void performInput(char input) throws ProgramTerminatedException {
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
    protected void test(String description, boolean expression, Object... arguments) {
        if (expression) {
            testsPassed++;
        } else {
            testsFailed++;
            if (arguments.length > 0)
                System.err.printf("  %s (%s) failed.%n", description, Arrays.toString(arguments));
            else
                System.err.printf("  %s failed.%n", description);
        }
    }

    /**
     * Tests if the given predicate holds.
     *
     * @param expression predicate to check
     *
     * @see BlackBox#test(String, boolean)
     */
    protected void test(boolean expression, Object... arguments) {
        test("Test", expression, arguments);
    }

    /**
     * This method implements the Edge 'e_SelectChallenge'
     *
     */
    public void e_SelectChallenge() throws ProgramTerminatedException {
        performInput('c');
    }


    /**
     * This method implements the Edge 'e_SelectContinue'
     *
     */
    public void e_SelectContinue() throws ProgramTerminatedException {
        performInput('c');
    }


    /**
     * This method implements the Edge 'e_SelectHint'
     *
     */
    public void e_SelectHint() throws ProgramTerminatedException {
        performInput('h');
    }


    /**
     * This method implements the Edge 'e_SelectNo'
     *
     */
    public void e_SelectNo() throws ProgramTerminatedException {
        performInput('n');
    }


    /**
     * This method implements the Edge 'e_SelectPeg'
     *
     */
    public void e_SelectPeg() throws ProgramTerminatedException, InvalidDataException {
        Integer mode = Integer.valueOf(getMbt().getDataValue("n"));
        char input = PEGS[random.nextInt(mode + 2)];
        performInput(input);
    }


    /**
     * This method implements the Edge 'e_SelectStandard'
     *
     */
    public void e_SelectStandard() throws ProgramTerminatedException {
        performInput('s');
    }


    /**
     * This method implements the Edge 'e_SelectStartNew'
     *
     */
    public void e_SelectStartNew() throws ProgramTerminatedException {
        performInput('s');
    }


    /**
     * This method implements the Edge 'e_SelectStats'
     *
     */
    public void e_SelectStats() throws ProgramTerminatedException {
        performInput('s');
    }


    /**
     * This method implements the Edge 'e_SelectYes'
     *
     */
    public void e_SelectYes() throws ProgramTerminatedException {
        performInput('y');
    }


    /**
     * This method implements the Edge 'e_StartProgram'
     *
     */
    public void e_StartProgram() throws IOException, ProgramTerminatedException {
        run(new String[0]);
    }


    /**
     * This method implements the Vertex 'v_FinalPeg'
     *
     */
    public void v_FinalPeg() throws ProgramTerminatedException {
        String[] output = readAndPrintLines();
        if (output.length > 0 && output[4].startsWith("Would you like to continue or start a new game or get a hint?"))
            getMbt().setCurrentVertex("v_GuessResult");
        else if (output.length > 0 && output[5].startsWith("It took you "))
            getMbt().setCurrentVertex("v_MainMenu");
            //TODO: test number of guesses
        else
            test("Test v_FinalPeg", false);
    }


    /**
     * This method implements the Vertex 'v_GuessResult'
     *
     */
    public void v_GuessResult() throws ProgramTerminatedException {
        //What should we test here?
    }


    /**
     * This method implements the Vertex 'v_MainMenu'
     *
     */
    public void v_MainMenu() throws ProgramTerminatedException {
        //Problem: what if set to Main Menu through win game?
        String[] output = readAndPrintLines();
        test("Test v_MainMenu", output.length > 0 && output[0].startsWith("Ready to start a new game?"));

        //OR (if chosen show statistics)
        //String[] output = bb.readAndPrintLines();
        //bb.test("Test v_MainMenu", output.length > 0 && output[1].startsWith("MasterMind Statistics"));
    }


    /**
     * This method implements the Vertex 'v_ModeMenu'
     *
     */
    public void v_ModeMenu() throws ProgramTerminatedException {
        String[] output = readAndPrintLines();
        test("Test v_ModeMenu", output.length > 0 && output[0].startsWith("Please choose: "));
    }


    /**
     * This method implements the Vertex 'v_RequestPeg'
     *
     */
    public void v_RequestPeg() throws ProgramTerminatedException, InvalidDataException {
        //How to check first peg?
        String[] output = readAndPrintLines();
        if (Integer.valueOf(getMbt().getDataValue("n")) > 1)
            test("Test v_ModeMenu", output.length > 0 && output[0].startsWith("Color of peg "));
    }


    /**
     * This method implements the Vertex 'v_Stopped'
     *
     */
    public void v_Stopped() throws ProgramTerminatedException {
        String[] output = readAndPrintLines();
        test("Test v_Stopped", output.length > 0 && output[0].startsWith("Thank you for playing! Bye!"));
    }



    public static void main(String[] args) throws URISyntaxException, StopConditionException, InterruptedException {
        ModelHandler handler = new ModelHandler();

        URL url = MasterMindModel.class.getResource("/MasterMindModel.graphml");
        File file = new File(url.toURI());

        handler.add("Play", new MasterMindModel(file, new A_StarPathGenerator(new EdgeCoverage(1.0))));
        handler.execute("Play");

        System.out.println(handler.getStatistics());
    }


}

