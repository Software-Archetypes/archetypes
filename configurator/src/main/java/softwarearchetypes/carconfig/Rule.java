package softwarearchetypes.carconfig;

import softwarearchetypes.sat.Clause;

import java.util.List;

public interface Rule {

    List<Clause> toClause();
}

