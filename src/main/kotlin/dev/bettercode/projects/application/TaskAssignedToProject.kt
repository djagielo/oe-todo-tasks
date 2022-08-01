package dev.bettercode.projects.application

import dev.bettercode.projects.ProjectId
import dev.bettercode.tasks.TaskId
import dev.bettercode.commons.events.DomainEvent

data class TaskAssignedToProject(val taskId: TaskId, val projectId: ProjectId) : DomainEvent()