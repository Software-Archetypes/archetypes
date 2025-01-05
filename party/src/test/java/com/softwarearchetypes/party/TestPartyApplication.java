package com.softwarearchetypes.party;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestPartyApplication {

    public static void main(String[] args) {
        SpringApplication.from(PartyApplication::main).with(TestPartyApplication.class).run(args);
    }

}
