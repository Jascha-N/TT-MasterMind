import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MasterMindTest extends BlackBox {

    private static final Random random = new Random();
    private static final String[] pegs = {"red", "orange", "yellow", "green", "blue", "indigo", "violet"};

    private void testRandomInvalidInputs(String[] validInputs) {
        testRandomInvalidInputs(validInputs, 5);
    }

    private void testRandomInvalidInputs(String[] validInputs, int count) {
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
        String[] lines = readLines();
        printOutput(lines);
        test(lines.length > 0 && lines[0].equals("Error in reading your input."));
    }

    private void testWelcomeScreen() {
        String[] lines = readLines();
        printOutput(lines);
        test(lines.length > 0);
        test(lines[0].startsWith("Welcome to MasterMind"));
        test(lines[lines.length - 1].startsWith("Ready to start"));

        testInvalidInput("c");
        testInvalidInput("x");
        testRandomInvalidInputs(new String[]{"y", "n", "s"});
    }

    @Override
    protected void performTests() throws Exception {
        testWelcomeScreen();
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
