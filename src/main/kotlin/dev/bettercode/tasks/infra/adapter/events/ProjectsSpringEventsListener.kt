package dev.bettercode.tasks.infra.adapter.events

import dev.bettercode.tasks.application.projects.ProjectCompleted
import dev.bettercode.tasks.application.projects.ProjectDeleted
import dev.bettercode.tasks.application.projects.ProjectReopened
import dev.bettercode.tasks.application.tasks.ProjectCompletedHandler
import dev.bettercode.tasks.application.tasks.ProjectDeletedHandler
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