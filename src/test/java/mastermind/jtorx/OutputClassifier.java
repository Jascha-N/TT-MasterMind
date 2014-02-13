package mastermind.jtorx;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputClassifier {
    private final Pattern pattern;
    private final String label;
    private final TestCase[] testCases;

    public OutputClassifier(String label, String pattern, TestCase... testCases) {
        this.pattern = Pattern.compile(pattern);
        this.label = label;
        this.testCases = testCases;
    }

    public int run(CharSequence output) {
        Matcher matcher = pattern.matcher(output);
        if (matcher.lookingAt()) {
            for (int i = 0; i < testCases.length; i++) {
                int[] groups = testCases[i].getGroups();
                String[] arguments = new String[groups.length];
                for (int j = 0; j < arguments.length; j++) {
                    arguments[j] = matcher.group(groups[j]);
                }
                if (!testCases[i].run(arguments)) {
                    System.out.printf("A_LOG Test case %d for label %s failed with arguments: %s.%n", i, label, Arrays.toString(arguments));
                }
            }
            return matcher.group().length();
        }

        return -1;
    }

    public String getLabel() {
        return label;
    }

}
