package com.softwarearchetypes.accounting;

import org.springframework.boot.SpringApplication;

public class TestAccountingApplication {

	public static void main(String[] args) {
		SpringApplication.from(AccountingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
