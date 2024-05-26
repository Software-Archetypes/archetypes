package com.bartslota.availability.domain


import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric

class AssetIdFixture {

    static AssetId someAssetId() {
        AssetId.of(someAssetIdValue())
    }

    static String someAssetIdValue() {
        randomAlphanumeric(10)
    }
}
