package softwarearchetypes.sat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DPLLSolver {

    public boolean solve(List<Clause> clauses, Map<Integer, Boolean> assignment) {

        if (clauses.stream().allMatch(clause ->
                clause.representation().stream().anyMatch(literal ->
                        assignment.containsKey(Math.abs(literal)) && assignment.get(Math.abs(literal)) == (literal > 0)))) {
            return true;
        }

        if (clauses.stream().anyMatch(clause ->
                clause.representation().stream().allMatch(literal ->
                        assignment.containsKey(Math.abs(literal)) && assignment.get(Math.abs(literal)) != (literal > 0)))) {
            return false;
        }

        Optional<Integer> unassigned = clauses
                .stream()
                .map(Clause::representation)
                .flatMap(List::stream)
                .map(Math::abs)
                .distinct()
                .filter(variable -> !assignment.containsKey(variable))
                .findFirst();

        if (unassigned.isEmpty()) {
            return false;
        }

        int variable = unassigned.get();

        Map<Integer, Boolean> assignmentWithTrue = new HashMap<>(assignment);
        assignmentWithTrue.put(variable, true);

        if (solve(clauses, assignmentWithTrue)) {
            return true;
        }

        Map<Integer, Boolean> assignmentWithFalse = new HashMap<>(assignment);
        assignmentWithFalse.put(variable, false);

        return solve(clauses, assignmentWithFalse);
    }
}