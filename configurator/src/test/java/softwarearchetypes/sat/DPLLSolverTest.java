package softwarearchetypes.sat;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DPLLSolverTest {

    DPLLSolver solver = new DPLLSolver();

    @Test
    public void testSolver() throws Exception {
        //expect
        assertTrue(isSolvable("12Variables_36Clauses_SAT.txt"));
        assertTrue(isSolvable("12Variables_36Clauses_SAT2.txt"));
        assertTrue(isSolvable("2Variables_3Clauses_SAT.txt"));
        assertTrue(isSolvable("23Variables_75Clauses_SAT.txt"));
        assertTrue(isSolvable("138Variables_519Clauses_SAT.txt"));

        assertFalse(isSolvable("1Variable_2Clauses_UNSAT.txt"));
        assertFalse(isSolvable("1Variable_2Clauses_UNSAT.txt"));
        assertFalse(isSolvable("3Variables_8Clauses_UNSAT.txt"));
        assertFalse(isSolvable("12Variables_36Clauses_UNSAT.txt"));
        assertFalse(isSolvable("47Variables_324Clauses_UNSAT.txt"));
        assertFalse(isSolvable("138Variables_519Clauses_UNSAT.txt"));
    }


    boolean isSolvable(String file) throws Exception {
        return solver.solve(clauses(file), new HashMap<>());
    }

    List<Clause> clauses(String fileName) throws Exception {
        List<Clause> clauses = new ArrayList<>();
        List<String> lines = readAllLines(Paths.get(Paths.get(getClass().getClassLoader().getResource(fileName).toURI()).toString()));
        for (String line : lines) {
            List<Integer> oneClause = stream(line.split(" "))
                    .filter(oneLine -> !oneLine.isEmpty())
                    .map(Integer::parseInt)
                    .collect(toList());
            clauses.add(new Clause(oneClause));
        }
        return clauses;
    }
}