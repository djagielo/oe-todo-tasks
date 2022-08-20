package dev.bettercode.shared

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@ContextConfiguration(initializers = [IntegrationTestBase.EnvInitializer::class])
open class IntegrationTestBase {

    companion object {
        fun rabbitMQContainer(): RabbitMQContainer {
            return RabbitMQContainer("rabbitmq:3.7.25-management-alpine")
                .withNetworkAliases("rabbitmq")
                .withUser("admin", "admin")
                .withPermission("/", "admin", ".*", ".*", ".*")
                .withExchange("oe-todo-tasks.projectCreated", "fanout")
                .withExchange("oe-todo-tasks.projectDeleted", "fanout")
                .withExchange("oe-todo-tasks.taskCreated", "fanout")
                .withQueue("audit")
                .withQueue("dynamic-projects")
                .withQueue("karma")
                .withQueue("tasks")
                .withBinding("oe-todo-tasks.projectDeleted", "tasks")
                .withExposedPorts(5672)
        }
    }

    class EnvInitializer :
        ApplicationContextInitializer<ConfigurableApplicationContext?> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=jdbc:tc:mariadb:10.6:///",
                "spring.rabbitmq.host=localhost",
            ).applyTo(applicationContext)
        }
    }
}
