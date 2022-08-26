package dev.bettercode.tasks.shared

import dev.bettercode.commons.events.DomainEvent
import dev.bettercode.projects.application.ProjectCreated
import dev.bettercode.projects.application.ProjectDeleted
import dev.bettercode.tasks.application.TaskCreated
import dev.bettercode.tasks.infra.adapter.rabbit.listener.ProjectDeletedEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate

class RabbitEventPublisher(private val rabbitTemplate: RabbitTemplate) : DomainEventPublisher {
    private val mapping = mapOf(
        ProjectCreated::class to "projectCreated",
        ProjectDeleted::class to "projectDeleted",
        TaskCreated::class to "taskCreated",
    )

    override fun publish(event: DomainEvent) {
        mapping[event::class]?.let {
            rabbitTemplate.convertAndSend("oe-todo", it, convert(event))
        }
    }

    private fun convert(event: DomainEvent): Any {
        return when (event) {
            is ProjectDeleted -> ProjectDeletedEvent(projectId = event.projectId.uuid.toString(), forced = event.forced)
            else -> event
        }
    }
}