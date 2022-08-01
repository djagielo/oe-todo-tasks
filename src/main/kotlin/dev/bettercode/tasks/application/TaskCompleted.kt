package dev.bettercode.tasks.application

import dev.bettercode.tasks.TaskId
import dev.bettercode.commons.events.DomainEvent

data class TaskCompleted(val taskId: TaskId) : DomainEvent()