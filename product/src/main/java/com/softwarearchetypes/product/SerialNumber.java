package com.softwarearchetypes.product;

interface SerialNumber {

    String type();

    String value();

    static SerialNumber of(String value) {
        return TextualSerialNumber.of(value);
    }

    static SerialNumber vin(String value) {
        return VinSerialNumber.of(value);
    }

    static SerialNumber imei(String value) {
        return ImeiSerialNumber.of(value);
    }
}
