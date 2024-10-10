package com.softwarearchetypes.pricing.domain;

import java.util.UUID;

public interface PricingRepository {

    UUID save(Pricing pricing);
}
