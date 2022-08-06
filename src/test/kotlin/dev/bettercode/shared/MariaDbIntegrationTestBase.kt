package dev.bettercode.shared

import dev.bettercode.componentTests.TasksServiceComponentTests
import dev.bettercode.tasks.integration.TasksIntegrationTestBase
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
                "spring.datasource.url=${db.jdbcUrl}",
            ).applyTo(applicationContext)
        }
    }

    companion object {
        @Container
        private val db: MariaDBContainer<*> = TasksServiceComponentTests.KMariaDBContainer("mariadb:10.6")
            .withNetworkAliases("mariadb").withExposedPorts(3306).withEnv(
                mapOf(
                    "MARIADB_ROOT_PASSWORD" to "password"
                )
            )
    }
}
