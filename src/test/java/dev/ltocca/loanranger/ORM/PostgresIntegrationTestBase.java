// File: src/test/java/dev/ltocca/loanranger/ORM/PostgresIntegrationTestBase.java
package dev.ltocca.loanranger.ORM;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 *  Singleton class used to decouple the lifecycle of the container from the JUnit runner, starting it only once
 *  and stopping it when the JVM exits. This way we can share the container between tests.
 */

public abstract class PostgresIntegrationTestBase {


    static final PostgreSQLContainer<?> postgresqlContainer;

    // static initializer block to create and start the container only once when this class is loaded by the JVM.
    static {
        postgresqlContainer = new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("loanranger_test_singleton")
                .withUsername("test")
                .withPassword("test");

        // Start the container manually, will be stopped automatically when the test process exits.
        postgresqlContainer.start();
    }

    // used by subclass to configure its Spring ApplicationContext.
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }
}