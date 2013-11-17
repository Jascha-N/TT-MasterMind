import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MasterMindTest extends BlackBox {

    private static final Random random = new Random();
    private static final char[] PEGS = {'r', 'o', 'y', 'g', 'b', 'i'};

    // UTILITY FUNCTIONS

//    private void testRandomInvalidInputs(String... validInputs) {
//        testRandomInvalidInputs(5, validInputs);
//    }
//
//    private void testRandomInvalidInputs(int count, String... validInputs) {
//        Arrays.sort(validInputs);
//
//        for (int i = 0; i < count; i++) {
//            int len = random.nextInt(100);
//            String str;
//            do {
//                char[] buf = new char[len];
//                for (int j = 0; j < buf.length; j++) {
//                    buf[j] = (char) (random.nextInt(128 - 32) + 32);
//                }
//                str = new String(buf);
//            } while(Arrays.binarySearch(validInputs, str) >= 0);
//
//            testInvalidInput(str);
//        }
//    }
//
//    private void testInvalidInput(String input) {
//        performInput(input);
//        String[] output = readLines();
//        printOutput(output);
//        test(output.length > 0 && output[0].equals("Error in reading your input."));
//    }
//
//    private void testWelcomeScreen() {
//        String[] output = readLines();
//        printOutput(output);
//        test(output.length > 0);
//        test(output[0].startsWith("Welcome to MasterMind"));
//        test(output[output.length - 1].startsWith("Ready to start"));
//
//        testInvalidInput("c");
//        testInvalidInput("x");
//        testRandomInvalidInputs("y", "n", "s");
//    }
//
//    private String performRandomInput(String... inputs) {
//        String input = inputs[random.nextInt(inputs.length)];
//        performInput(input);
//        return input;
//    }
//

    /**
     * Input a randomly selected input from a list of given inputs and return it.
     *
     * @param inputs list of inputs to randomly choose from
     * @return the selected peg
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    private char performRandomInput(char... inputs) throws ProgramTerminatedException {
        char input = inputs[random.nextInt(inputs.length)];
        performInput(input);
        return input;
    }

    /**
     * Input a randomly selected peg and return it.
     *
     * @return the selected peg
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    private char inputRandomPeg() throws ProgramTerminatedException {
        return performRandomInput(PEGS);
    }

    /**
     * Tests a single game of MasterMind.
     *
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    private void testGame() throws ProgramTerminatedException {
        boolean solutionFound = false;
        // Collects the chosen pegs to be checked at the end of a guess; i.e. "r o y g"
        int guess = 0;

        // Test Case ID: 6 & 9
        String firstOutput = "", curOutput = "";
        while(!solutionFound && guess < 3) {
            StringBuilder inputs = new StringBuilder();
            for (int j = 1; j <= 4; j++) {
                char input = PEGS[j];
                if (guess == 2 && j == 1) // Test Case ID: 9 - change one input
                    input = PEGS[5];
                performInput(input);
                inputs.append(' ').append(input);
                String[] output = readAndPrintLines();

                String description = "Test 5 (step " + j + ")";
                if (j < 4)
                    test(description, output.length > 0 && output[0].startsWith("Color of peg " + (j + 1)));
                else {
                    test(description, output.length > 4 &&
                            output[1].contains("Your guess:"+inputs.toString()) &&
                            output[3].matches("\\s*Your hits: [Hhm] [Hhm] [Hhm] [Hhm].*"));

                    if (!output[4].startsWith("Would you like")) {
                        solutionFound = true;
                        Matcher matcher = Pattern.compile("It took you (\\d+) guesses.").matcher(output[5]);
                        test("Test 5b (step 4)", matcher.matches());
                    }
                    else {
                        performInput('c');
                        output = readAndPrintLines();
                        String [] splitOutput = output[guess+2].split("\\:");
                        curOutput = splitOutput[1];
                    }
                }
            }

            if (guess == 0)
                firstOutput = curOutput;
            else if (guess == 1) // Test Case ID: 6
                test("Test 6", firstOutput.equals(curOutput));
            else { // Test Case ID: 9
                int diff = 0, x = 12, y = 12;
                //Find how many chars are different
                //Note that chars are sorted, so Hamming distance is insufficient
                while (x < firstOutput.length() && y < firstOutput.length()) {
                    if (firstOutput.charAt(x) > curOutput.charAt(y)) {
                        diff++;
                        y = y + 2;
                    }
                    else if (firstOutput.charAt(x) < curOutput.charAt(y)) {
                        diff++;
                        x = x + 2;
                    }
                    else {
                        x = x + 2;
                        y = y + 2;
                    }
                }
                
                test("Test 9", diff < 2);
            }
            guess++;
        }

        while(!solutionFound) {// Randomly guessing until it finds the answer actually does work
            StringBuilder inputs = new StringBuilder();
            // Test Case ID: 4
            {
                char input = PEGS[guess % PEGS.length]; // Rotate the chosen peg, so that each one is chosen at least once.
                performInput(input);
                inputs.append(input);
                String[] output = readAndPrintLines();
                test("Test 4", output.length > 0 && output[0].startsWith("Color of peg 2"));
            }

            // Test Case ID: 5
            for (int j = 2; j <= 4; j++) {
                char input = inputRandomPeg();
                inputs.append(' ').append(input);
                String[] output = readAndPrintLines();

                String description = "Test 5 (step " + j + ")";
                if (j < 4)
                    test(description, output.length > 0 && output[0].startsWith("Color of peg " + (j + 1)));
                else {
                    test(description, output.length > 4 &&
                            output[1].contains("Your guess: "+inputs.toString()) &&
                            output[3].matches("\\s*Your hits: [Hhm] [Hhm] [Hhm] [Hhm].*"));

                    if (!output[4].startsWith("Would you like")) {
                        solutionFound = true;
                        Matcher matcher = Pattern.compile("It took you (\\d+) guesses.").matcher(output[5]);
                        test("Test 5b (step 4)", matcher.matches());

                        // Extra test case? Check if number of guesses is correct?
                        // int guesses = Integer.parseInt(matcher.group(1));
                        // test("...", guesses == guess + 1)
                    }
                }
            }

            if (!solutionFound) {
                // Test Case ID: 7
                performInput('c');
                String [] output = readAndPrintLines();
                test("Test 7", output[1].contains("Summary of guesses"));
                test("Test 7_2", output[guess+2].contains("Guess " + (guess+1) + ": " + inputs.toString()));
            }

            guess++;
        }
    }

    /* Test a single input
     * @param test The test case ID
     * @param input The character to input
     * @parm msg (Part of) the expected output
     * @throws ProgramTerminatedException when the tested program has terminated
     */
    protected void inputTest(int test, char input, String msg) throws ProgramTerminatedException {
        performInput('q');
        String[] output = readAndPrintLines();
        test("Test 10", output.length > 0 && output[0].startsWith("Error in reading"));
        performInput(input);
        output = readAndPrintLines();
        test("Test " + test, output.length > 0 && output[0].startsWith(msg));
    }

    // This is where the tests go.
    @Override
    protected void performTests() throws ProgramTerminatedException {
        readAndPrintLines(); // Consume and print welcome screen.

        inputTest(2,'s',"****************");
        inputTest(2,'y',"Please choose: s = standard mode");
        inputTest(3,'c',"Please choose the colors for 5 holes from");

        inputTest(4,'r',"Color of peg 2");
        inputTest(4,'o',"Color of peg 3");
        inputTest(4,'y',"Color of peg 4");
        inputTest(4,'i',"Color of peg 5");
        inputTest(4,'v',"\n"); //What string to compare it with? "\n" and " " are wrong
        inputTest(7,'s',"Ready to start a new game?");

        inputTest(2,'y',"Please choose: s = standard mode");
        inputTest(3,'s',"Please choose four colors from");

        testGame();
    }

    // Runs the game of MasterMind.
    @Override
    protected void runProgram(String[] args) throws Exception {
        MasterMind.main(args);
    }

    /**
     * Main entry point for testing.
     *
     * @param args program arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BlackBox blackBox = new MasterMindTest();
        blackBox.run(args);
    }

}
