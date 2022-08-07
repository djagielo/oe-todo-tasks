package dev.bettercode.projects.application

import dev.bettercode.projects.ProjectId
import dev.bettercode.commons.events.DomainEvent

data class ProjectReopened(val projectId: ProjectId) : DomainEvent()