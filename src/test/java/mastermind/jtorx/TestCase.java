package mastermind.jtorx;

public abstract class TestCase {

    private final int[] groups;

    protected TestCase(int... groups) {
        this.groups = groups;
    }

    public abstract boolean run(String[] arguments);

    public int[] getGroups() {
        return groups;
    }

}
