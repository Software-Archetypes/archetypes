package softwarearchetypes.sat;

import java.util.List;

public record Clause(List<Integer> representation) {

    public Clause(Integer ... literals) {
        this(List.of(literals));
    }
}
