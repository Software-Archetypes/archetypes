package com.softwarearchetypes.pricing.common;

import org.junit.jupiter.api.Assertions;

public class ResultAssert {

    private final Result result;

    public ResultAssert(Result result) {
        this.result = result;
    }

    public ResultAssert isSuccess() {
        Assertions.assertTrue(result.success());
        return this;
    }

}
