package softwarearchetypes.carconfig;

import java.util.UUID;

public record CarConfigId(UUID id) {

    public static CarConfigId random() {
        return new CarConfigId(UUID.randomUUID());
    }
}

