package com.bartslota.availability

import groovy.transform.CompileStatic
import spock.util.concurrent.PollingConditions

@CompileStatic
class PredefinedPollingConditions {
    static final PollingConditions SHORT_WAIT = new PollingConditions(timeout: DEFAULT_SHORT)
    static final PollingConditions WAIT = new PollingConditions(timeout: DEFAULT_MEDIUM)
    static final PollingConditions LONG_WAIT = new PollingConditions(timeout: DEFAULT_LONG)
    static final PollingConditions SHORT_WAIT_WITH_INITIAL_DELAY = new PollingConditions(timeout: DEFAULT_SHORT + 1, initialDelay: 1)

    private static final int DEFAULT_SHORT = 3
    private static final int DEFAULT_MEDIUM = 10
    private static final int DEFAULT_LONG = 30
}
