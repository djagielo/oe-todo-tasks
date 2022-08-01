package dev.bettercode.projects.application

import dev.bettercode.commons.events.DomainEvent
import dev.bettercode.projects.ProjectId

data class ProjectDeleted(val projectId: ProjectId, val forced: Boolean): DomainEvent()