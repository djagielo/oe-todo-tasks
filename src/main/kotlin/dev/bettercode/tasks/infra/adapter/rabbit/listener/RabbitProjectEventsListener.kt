package dev.bettercode.tasks.infra.adapter.rabbit.listener

import dev.bettercode.tasks.TasksConfiguration
import dev.bettercode.tasks.application.ProjectDeletedHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener


internal class RabbitProjectEventsListener(private val projectDeletedHandler: ProjectDeletedHandler) {

    @RabbitListener(queues = ["tasks"], containerFactory = TasksConfiguration.RABBIT_LISTENER_FACTORY_NAME)
    fun projectDeleted(projectDeleted: ProjectDeletedEvent) {
        projectDeletedHandler.handle(projectDeleted)
    }
}