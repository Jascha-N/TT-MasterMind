import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MasterMindTest extends BlackBox {

    private static final Random random = new Random();
    //private static final int MAX_TURNS = Integer.MAX_VALUE;
    private static final char[] PEGS = {'r', 'o', 'y', 'g', 'b', 'i'};

    private void testRandomInvalidInputs(String... validInputs) {
        testRandomInvalidInputs(5, validInputs);
    }

    private void testRandomInvalidInputs(int count, String... validInputs) {
        Arrays.sort(validInputs);

        for (int i = 0; i < count; i++) {
            int len = random.nextInt(100);
            String str;
            do {
                char[] buf = new char[len];
                for (int j = 0; j < buf.length; j++) {
                    buf[j] = (char) (random.nextInt(128 - 32) + 32);
                }
                str = new String(buf);
            } while(Arrays.binarySearch(validInputs, str) >= 0);

            testInvalidInput(str);
        }
    }

    private void testInvalidInput(String input) {
        performInput(input);
        String[] output = readLines();
        printOutput(output);
        test(output.length > 0 && output[0].equals("Error in reading your input."));
    }

    private void testWelcomeScreen() {
        String[] output = readLines();
        printOutput(output);
        test(output.length > 0);
        test(output[0].startsWith("Welcome to MasterMind"));
        test(output[output.length - 1].startsWith("Ready to start"));

        testInvalidInput("c");
        testInvalidInput("x");
        testRandomInvalidInputs("y", "n", "s");
    }

    private String performRandomInput(String... inputs) {
        String input = inputs[random.nextInt(inputs.length)];
        performInput(input);
        return input;
    }

    private char performRandomInput(char... inputs) {
        char input = inputs[random.nextInt(inputs.length)];
        performInput(input);
        return input;
    }

    private char inputRandomPeg() {
        return performRandomInput(PEGS);
    }

    // Play one game of mastermind
    private void testGame() {
        boolean solutionFound = false;
        int guess = 0;
        do {
            StringBuilder inputs = new StringBuilder();

            // Test Case ID: 4
            {
                char input = PEGS[guess % PEGS.length];
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
                    test("Test 5 (step 4)", output.length > 4 &&
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
                // TODO: Test Case ID: 7
                performInput('c');
                readAndPrintLines();
            }
        } while(!solutionFound); // TODO: Randomly guessing until it finds the answer is not going to work
    }

    @Override
    protected void performTests() throws Exception {
        readAndPrintLines(); // Print welcome screen

        // Test Case ID: 2
        {
            performInput('y');
            String[] output = readAndPrintLines();
            test("Test 2", output.length > 0 && output[0].startsWith("Please choose: s = standard mode"));
        }

        // Test Case ID: 3
        {
            performInput('s');
            String[] output = readAndPrintLines();
            test("Test 3", output.length > 0 && output[0].startsWith("Please choose four colors from"));
        }

        testGame();
    }

    @Override
    protected void runProgram(String[] args) throws Exception {
        MasterMind.main(args);
    }

    public static void main(String[] args) throws IOException {
        BlackBox blackBox = new MasterMindTest();
        blackBox.run(args);
    }

}
