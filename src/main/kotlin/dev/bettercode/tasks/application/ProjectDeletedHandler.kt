package dev.bettercode.tasks.application

import dev.bettercode.projects.application.ProjectDeleted
import dev.bettercode.projects.application.ProjectService
import dev.bettercode.tasks.domain.TasksRepository
import dev.bettercode.tasks.query.TasksQueryService
import org.springframework.data.domain.Pageable

internal class ProjectDeletedHandler(
    private val tasksRepository: TasksRepository,
    private val tasksQueryService: TasksQueryService,
    private val projectService: ProjectService,
    private val projectAssignmentService: ProjectAssignmentService
) {
    fun handle(projectDeleted: ProjectDeleted) {
        // as for now let's support only removing up to 100 elements
        val tasksPage = tasksQueryService.findAllForProject(Pageable.ofSize(100), projectDeleted.projectId)
        if (projectDeleted.forced) {
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