package dev.bettercode.tasks.application

import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.application.ProjectDeleted
import dev.bettercode.projects.application.ProjectService
import dev.bettercode.tasks.domain.TasksRepository
import dev.bettercode.tasks.infra.adapter.rabbit.listener.ProjectDeletedEvent
import dev.bettercode.tasks.query.TasksQueryService
import org.springframework.data.domain.Pageable
import java.util.*

internal class ProjectDeletedHandler(
    private val tasksRepository: TasksRepository,
    private val tasksQueryService: TasksQueryService,
    private val projectService: ProjectService,
    private val projectAssignmentService: ProjectAssignmentService
) {
    fun handle(projectDeleted: ProjectDeleted) {
        handle(projectId = projectDeleted.projectId, forced = projectDeleted.forced)
    }

    fun handle(projectDeleted: ProjectDeletedEvent) {
        handle(projectId = ProjectId(uuid = UUID.fromString(projectDeleted.projectId!!)), forced = projectDeleted.forced!!)
    }

    private fun handle(projectId: ProjectId, forced: Boolean) {
        // as for now let's support only removing up to 100 elements
        val tasksPage = tasksQueryService.findAllForProject(Pageable.ofSize(100), projectId)
        if (forced) {
            tasksPage.forEach {
                tasksRepository.delete(it.id)
            }
        } else {
            tasksPage.forEach {
                projectAssignmentService.assign(it.id, projectService.getInboxProject().id)
            }
        }
    }
}