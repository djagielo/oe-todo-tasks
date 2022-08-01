package dev.bettercode.tasks.application

import dev.bettercode.tasks.TaskId
import dev.bettercode.commons.events.DomainEvent

data class TaskReopened(val taskId: TaskId) : DomainEvent()