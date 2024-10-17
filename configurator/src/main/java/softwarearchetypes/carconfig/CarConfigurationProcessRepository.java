package softwarearchetypes.carconfig;

import java.util.HashMap;
import java.util.Map;

public class CarConfigurationProcessRepository {
    private final Map<CarConfigProcessId, CarConfigurationProcess> processes = new HashMap<>();

    public CarConfigurationProcess load(CarConfigProcessId carConfigProcessId) {
        CarConfigurationProcess process = processes.get(carConfigProcessId);
        if(process == null) throw new IllegalArgumentException("Cannot find process with given id");
        return process;
    }

    public void addProcess(CarConfigProcessId carConfigProcessId, CarConfigurationProcess carConfigurationProcess) {
        processes.put(carConfigProcessId, carConfigurationProcess);
    }
}
