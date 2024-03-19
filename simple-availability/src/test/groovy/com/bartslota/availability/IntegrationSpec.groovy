package com.bartslota.availability

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integrationTest")
@Testcontainers
abstract class IntegrationSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @LocalServerPort
    int port

    @Shared
    def static postgres = new PostgreSQLContainer("postgres:15.3")
            .withDatabaseName("availability_db")
            .withUsername("test_user")
            .withPassword("test_user_password")
            .withInitScript("testContainers/postgresContainer.sql")

    @DynamicPropertySource
    static void postgresProps(DynamicPropertyRegistry registry) {
        postgres.start()
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
        registry.add("spring.liquibase.url", postgres::getJdbcUrl)
        registry.add("spring.liquibase.user", postgres::getUsername)
        registry.add("spring.liquibase.password", postgres::getPassword)
    }
}
