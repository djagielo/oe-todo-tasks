package dev.bettercode.tasks.shared

import dev.bettercode.commons.events.DomainEvent
import dev.bettercode.projects.application.ProjectCreated
import dev.bettercode.tasks.application.TaskCreated
import org.springframework.amqp.rabbit.core.RabbitTemplate

class RabbitEventPublisher(private val rabbitTemplate: RabbitTemplate): DomainEventPublisher {
    private val mapping = mapOf(
        ProjectCreated::class to "oe-todo-tasks.projectCreated",
        TaskCreated::class to "oe-todo-tasks.taskCreated"
    )
    override fun publish(event: DomainEvent) {
        mapping[event::class]?.let {
            rabbitTemplate.convertAndSend(it, "", "$event")
        }
    }
}