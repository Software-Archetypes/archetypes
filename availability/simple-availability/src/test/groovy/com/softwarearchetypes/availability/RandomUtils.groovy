package com.softwarearchetypes.availability


import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic

class RandomUtils {

    static String random(String placeholder) {
        "$placeholder-${randomAlphabetic(10)}"
    }
}
