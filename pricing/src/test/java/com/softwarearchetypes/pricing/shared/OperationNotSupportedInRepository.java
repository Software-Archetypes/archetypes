package com.softwarearchetypes.pricing.shared;

class OperationNotSupportedInRepository extends RuntimeException {

    public OperationNotSupportedInRepository(String message) {
        super(message);
    }
}
