import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Banker's algorithm with defensive state copies and deterministic tracing. */
public final class BankersAlgorithm {
    private int[][] allocation;
    private final int[][] max;
    private int[][] need;
    private int[] available;
    private String requestVerdict;

    /** Presentation text for the caller to print after its state checks. */
    public String requestVerdict() { return requestVerdict; }

    public record SafetyResult(boolean safe, List<Integer> sequence,
                               List<Integer> stuck, List<Integer> finalWork) {
        public SafetyResult {
            sequence = List.copyOf(sequence);
            stuck = List.copyOf(stuck);
            finalWork = List.copyOf(finalWork);
        }
    }

    public BankersAlgorithm(int[][] allocation, int[][] max, int[] available,
                            int[] total) {
        if (allocation == null || max == null || available == null || total == null
                || allocation.length == 0 || available.length == 0
                || allocation.length != max.length || available.length != total.length) {
            throw new IllegalArgumentException("Invalid or mismatched state dimensions");
        }
        validateVector(available, available.length, "Available");
        validateVector(total, available.length, "Total");
        need = new int[allocation.length][available.length];
        for (int i = 0; i < allocation.length; i++) {
            validateVector(allocation[i], available.length, "Allocation row");
            validateVector(max[i], available.length, "Max row");
            for (int j = 0; j < available.length; j++) {
                if (allocation[i][j] > max[i][j]) {
                    throw new IllegalArgumentException("Allocation exceeds Max for P" + (i + 1));
                }
                need[i][j] = max[i][j] - allocation[i][j];
            }
        }
        this.allocation = copy(allocation);
        this.max = copy(max);
        this.available = available.clone();
        for (int j = 0; j < total.length; j++) {
            long sum = available[j];
            for (int[] row : allocation) sum += row[j];
            if (sum != total[j]) {
                System.out.printf("WARNING: resource %s: Allocation + Available = %d, Total = %d%n",
                        resource(j), sum, total[j]);
            }
        }
    }

    private static void validateVector(int[] vector, int size, String name) {
        if (vector == null || vector.length != size) {
            throw new IllegalArgumentException(name + " dimension mismatch");
        }
        for (int value : vector) {
            if (value < 0) throw new IllegalArgumentException(name + " contains a negative value");
        }
    }

    private static int[][] copy(int[][] matrix) {
        return Arrays.stream(matrix).map(int[]::clone).toArray(int[][]::new);
    }

    public static String vector(int[] values) {
        return Arrays.toString(values).replace("[", "<").replace("]", ">").replace(" ", "");
    }

    public static String sequence(List<Integer> processes) {
        return "<" + String.join(",", processes.stream().map(i -> "P" + (i + 1)).toList()) + ">";
    }

    private static String resource(int j) {
        return j < 26 ? Character.toString((char) ('A' + j)) : "R" + (j + 1);
    }

    public void printState() {
        String resources = String.join(",", java.util.stream.IntStream.range(0, available.length)
                .mapToObj(BankersAlgorithm::resource).toList());
        System.out.printf("%-8s %s%n", "Process", "Need(" + resources + ")");
        for (int i = 0; i < allocation.length; i++) {
            System.out.printf("P%-7d %s%n", i + 1, tableValues(need[i]));
        }
    }

    private static String tableValues(int[] values) {
        String text = vector(values);
        return text.substring(1, text.length() - 1);
    }

    private void printPretend(int i) {
        System.out.printf("Need after pretend-allocation: P%d %s%n", i + 1, vector(need[i]));
    }

    /** Does not mutate state; each iteration restarts at P1 for lowest-index priority. */
    public SafetyResult safetyTest() {
        int[] work = available.clone();
        boolean[] finish = new boolean[allocation.length];
        List<Integer> order = new ArrayList<>();
        while (order.size() < allocation.length) {
            int chosen = -1;
            for (int i = 0; i < allocation.length; i++) {
                if (!finish[i] && fits(need[i], work)) { chosen = i; break; }
            }
            if (chosen == -1) break;
            String before = vector(work);
            for (int j = 0; j < work.length; j++) {
                work[j] = Math.addExact(work[j], allocation[chosen][j]);
            }
            finish[chosen] = true;
            order.add(chosen);
            System.out.printf("Step %d: P%d (Need %s <= Work %s) -> Work %s, Finish %s%n",
                    order.size(), chosen + 1, vector(need[chosen]), before, vector(work), finishes(finish));
        }
        List<Integer> stuck = new ArrayList<>();
        for (int i = 0; i < finish.length; i++) if (!finish[i]) stuck.add(i);
        boolean safe = stuck.isEmpty();
        if (!safe) {
            System.out.println("No process can proceed at Work " + vector(work) + ":");
            List<String> blocked = new ArrayList<>();
            for (int i : stuck) {
                List<String> shortResources = new ArrayList<>();
                for (int j = 0; j < work.length; j++) {
                    if (need[i][j] > work[j]) shortResources.add(resource(j));
                }
                blocked.add("P" + (i + 1) + " short " + String.join(",", shortResources));
            }
            System.out.println("  " + String.join(" | ", blocked));
        }
        return new SafetyResult(safe, order, stuck, Arrays.stream(work).boxed().toList());
    }

    private static String finishes(boolean[] finish) {
        List<String> flags = new ArrayList<>();
        for (boolean value : finish) flags.add(value ? "T" : "F");
        return "[" + String.join(",", flags) + "]";
    }

    private static boolean fits(int[] request, int[] limit) {
        for (int j = 0; j < request.length; j++) if (request[j] > limit[j]) return false;
        return true;
    }

    private static String excess(int[] request, int[] limit) {
        List<String> reasons = new ArrayList<>();
        for (int j = 0; j < request.length; j++) {
            if (request[j] > limit[j]) reasons.add(resource(j) + ": " + request[j] + " > " + limit[j]);
        }
        return String.join(", ", reasons);
    }

    /** processIndex is zero-based: 0 denotes P1. */
    public boolean requestResources(int processIndex, int[] request) {
        if (processIndex < 0 || processIndex >= allocation.length) {
            throw new IllegalArgumentException("Process index out of range");
        }
        validateVector(request, available.length, "Request");
        int i = processIndex;
        if (!fits(request, need[i])) {
            requestVerdict = "VERDICT: DENIED, request exceeds Need (" + excess(request, need[i]) + ")";
            return false;
        }
        if (!fits(request, available)) {
            requestVerdict = "VERDICT: DENIED, request exceeds Available ("
                    + excess(request, available) + ")";
            return false;
        }
        // Snapshot every mutable array deeply, so rollback restores the exact prior state.
        int[][] oldAllocation = copy(allocation);
        int[][] oldNeed = copy(need);
        int[] oldAvailable = available.clone();
        for (int j = 0; j < request.length; j++) {
            available[j] -= request[j];
            allocation[i][j] += request[j];
            need[i][j] -= request[j];
        }
        printPretend(i);
        SafetyResult result = safetyTest();
        if (result.safe()) {
            requestVerdict = "VERDICT: GRANTED, sequence " + sequence(result.sequence());
            return true;
        }
        allocation = oldAllocation;
        need = oldNeed;
        available = oldAvailable;
        requestVerdict = "VERDICT: DENIED, resulting state is UNSAFE";
        return false;
    }

    public boolean isSafeSequence(int... order) {
        if (order == null || order.length != allocation.length) return false;
        int[] work = available.clone();
        boolean[] seen = new boolean[allocation.length];
        for (int i : order) {
            if (i < 0 || i >= allocation.length || seen[i] || !fits(need[i], work)) return false;
            seen[i] = true;
            for (int j = 0; j < work.length; j++) work[j] = Math.addExact(work[j], allocation[i][j]);
        }
        return true;
    }

    public boolean sameState(BankersAlgorithm other) {
        return other != null && Arrays.deepEquals(allocation, other.allocation)
                && Arrays.deepEquals(max, other.max) && Arrays.deepEquals(need, other.need)
                && Arrays.equals(available, other.available);
    }
}
