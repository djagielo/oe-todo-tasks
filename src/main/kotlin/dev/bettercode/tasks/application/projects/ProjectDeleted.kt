package dev.bettercode.tasks.application.projects

import dev.bettercode.commons.events.DomainEvent
import dev.bettercode.tasks.ProjectId

data class ProjectDeleted(val projectId: ProjectId, val forced: Boolean): DomainEvent()