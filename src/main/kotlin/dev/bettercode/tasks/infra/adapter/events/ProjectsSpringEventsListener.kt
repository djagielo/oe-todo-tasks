package dev.bettercode.tasks.infra.adapter.events

import dev.bettercode.projects.application.ProjectCompleted
import dev.bettercode.projects.application.ProjectDeleted
import dev.bettercode.projects.application.ProjectReopened
import dev.bettercode.tasks.application.ProjectCompletedHandler
import dev.bettercode.tasks.application.ProjectDeletedHandler
import org.springframework.context.event.EventListener

internal class ProjectsSpringEventsListener(private val projectDeletedHandler: ProjectDeletedHandler, private val projectCompletedHandler: ProjectCompletedHandler) {

    @EventListener
    fun handleProjectDeleted(projectDeleted: ProjectDeleted) {
        projectDeletedHandler.handle(projectDeleted)
    }

    @EventListener
    fun handleProjectCompleted(projectCompleted: ProjectCompleted) {
        projectCompletedHandler.handleProjectCompleted(projectCompleted)
    }

    @EventListener
    fun handleProjectReopened(projectReopened: ProjectReopened) {
        projectCompletedHandler.handleProjectReopened(projectReopened)
    }
}