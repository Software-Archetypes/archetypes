package softwarearchetypes.carconfig;

import java.util.UUID;

public record CarConfigProcessId(UUID id) {

    public static CarConfigProcessId random() {
        return new CarConfigProcessId(UUID.randomUUID());
    }
}
