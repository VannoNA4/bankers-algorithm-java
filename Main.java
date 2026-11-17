import java.util.List;

public final class Main {
    private static BankersAlgorithm baseline() {
        return new BankersAlgorithm(
                new int[][] {{0,1,0}, {2,0,0}, {3,0,2}, {2,1,1}, {0,0,2}},
                new int[][] {{7,5,3}, {3,2,2}, {9,0,2}, {2,2,2}, {4,3,3}},
                new int[] {3,3,2}, new int[] {10,5,7});
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        System.out.println("TEST 1: Baseline Safety (S0)");
        BankersAlgorithm state = baseline();
        state.printState();
        BankersAlgorithm.SafetyResult result = state.safetyTest();
        check(result.safe() && result.sequence().equals(List.of(1,3,0,2,4)), "Baseline sequence");
        check(state.isSafeSequence(1,3,4,2,0), "Assignment self-check sequence");
        System.out.println("VERDICT: SAFE, sequence " + BankersAlgorithm.sequence(result.sequence()));

        denied("2a", 1, new int[] {2,0,0});
        denied("2b", 2, new int[] {6,0,0});
        denied("2c", 4, new int[] {3,3,0});

        System.out.println("\nTEST 3: P2 requests <1,0,2>");
        state = baseline();
        check(state.requestResources(1, new int[] {1,0,2}), "Expected grant");
        check(!state.sameState(baseline()), "Grant must keep new state");
        check(state.isSafeSequence(1,3,0,2,4), "Granted-state safe sequence");
        System.out.println(state.requestVerdict());
    }

    private static void denied(String title, int process, int[] request) {
        System.out.println("\nTEST " + title + ": P" + (process + 1)
                + " requests " + BankersAlgorithm.vector(request));
        BankersAlgorithm state = baseline();
        check(!state.requestResources(process, request), "Expected denial");
        check(state.sameState(baseline()), "Denial must restore exact S0");
        System.out.println(state.requestVerdict());
    }
}
