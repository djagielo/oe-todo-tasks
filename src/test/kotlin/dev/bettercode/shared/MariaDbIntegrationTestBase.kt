package dev.bettercode.shared

import dev.bettercode.componentTests.TasksServiceComponentTests
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ContextConfiguration(initializers = [MariaDbIntegrationTestBase.EnvInitializer::class])
open class MariaDbIntegrationTestBase {
    class EnvInitializer :
        ApplicationContextInitializer<ConfigurableApplicationContext?> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=jdbc:tc:mariadb:10.6:///",
            ).applyTo(applicationContext)
        }
    }
}
