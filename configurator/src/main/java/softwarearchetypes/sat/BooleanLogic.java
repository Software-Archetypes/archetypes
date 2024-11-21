package softwarearchetypes.sat;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BooleanLogic {
    public boolean isFormulaSatisfied(List<Clause> clauses, Set<Integer> trueLiterals) {
        return clauses.stream().allMatch(clause ->
                clause.representation().stream().anyMatch(literal ->
                        (trueLiterals.contains(Math.abs(literal)) && literal > 0)
                                ||
                                (literal < 0 && !trueLiterals.contains(Math.abs(literal)))
                )
        );
    }

    public List<List<Integer>> getNeededLiterals(List<Clause> clauses, Set<Integer> trueLiterals) {
        return clauses.stream()
                .map(clause ->
                        clause.representation().stream()
                                .filter(literal ->
                                        (literal > 0 && !trueLiterals.contains(literal)) ||
                                                (literal < 0 && trueLiterals.contains(-literal))
                                )
                                .toList()
                )
                .filter(neededLiterals -> !neededLiterals.isEmpty())
                .toList();
    }
}
