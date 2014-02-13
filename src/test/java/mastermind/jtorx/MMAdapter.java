package mastermind.jtorx;

import java.util.Arrays;

public class MMAdapter extends Adapter {

    // This array must be sorted!
    private static final String[] inputLabels = {"?Blue", "?Challenge", "?Continue", "?Green", "?Hint", "?Indigo",
            "?StartNewGame", "?No", "?Orange", "?Red", "?Standard", "?Start", "?Stats", "?Violet", "?Yellow", "?Yes"};

    private static final OutputClassifier[] outputClassifiers = {
            new OutputClassifier("!BadInput"     , "Error in reading your? input[.]\n"), // Sometimes it says "you input"

            new OutputClassifier("!WelcomeScreen", "Welcome to MasterMind(?:.+\n){13}"),
            new OutputClassifier("!StartMenu"    , "\nReady to start.+\n"),
            new OutputClassifier("!ModeMenu"     , "Please choose: s = standard.+\n.+\n"),
            new OutputClassifier("!ColorMenu"    , "Please choose (the|four) colors.+\n.+\n"), // Check number of colors
            new OutputClassifier("!RequestPeg"   , "Color of peg (\\d): "),                   // Check number of peg
            new OutputClassifier("!GuessResult"  , "\n\\s+Your guess: (\\w) (\\w) (\\w) (\\w) (\\w)? ?\n" + // Save guesses and check number (?)
                    "\n\\s+Your hits: (\\w) (\\w) (\\w) (\\w) (\\w)? ?"),    // Save hits and check number (?)
            new OutputClassifier("!GuessMenu"    , "\nWould you like to continue(.+\n){4}"),
            new OutputClassifier("!GuessSummary" , "\\s+[*]+\n\\s+Summary of guesses:\n" +
                    "(?:\\s+Guess (\\d+): (\\w) (\\w) (\\w) (\\w) (\\w)?\\s+(\\w) (\\w) (\\w) (\\w) (\\w)? ?\n)+" +
                    "\\s+[*]+\n"),
            new OutputClassifier("!Hint"         , "Hint: There is at least one (\\w)? peg[.]\n"),
            new OutputClassifier("!NoMoreHints"  , "Hint: Sorry, you have already used all of your hints[.]\n"),
            new OutputClassifier("!Win"          , "Congratulations! You win!\n[*]+\nIt took you (\\d+) guesses[.]\n[*]+\n"),
            new OutputClassifier("!Stats"        , "[*]+\nMasterMind Statistics(\n.*)\n[*]{53}\n"),
            new OutputClassifier("!Goodbye"      , "Thank you for playing! Bye!\n")
    };

    public MMAdapter(long timeout) {
        super(timeout);
    }

    public MMAdapter() {
        this(1000);
    }

    @Override
    protected String getInputFromLabel(String label) {
        if (Arrays.binarySearch(inputLabels, label) < 0)
            return null;

        return label.substring(1, 2);
    }

    @Override
    protected OutputClassifier[] getOutputClassifiers() {
        return outputClassifiers;
    }

    @Override
    protected String[] getJavaArguments() {
        return new String[] {"-cp", System.getProperty("java.class.path"), "MasterMind"};
    }

    public static void main(String[] args) {
        MMAdapter adapter;
        if (args.length > 0)
            adapter = new MMAdapter(Long.parseLong(args[0]));
        else
            adapter = new MMAdapter();

        adapter.run();
    }

}
