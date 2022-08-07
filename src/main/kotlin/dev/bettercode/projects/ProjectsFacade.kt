package dev.bettercode.projects

import dev.bettercode.projects.application.ProjectCompletionService
import dev.bettercode.projects.application.ProjectService
import dev.bettercode.projects.domain.Project
import dev.bettercode.tasks.query.ProjectsQueryService
import dev.bettercode.tasks.shared.DomainResult
import org.springframework.data.domain.Page

open class ProjectsFacade internal constructor(
    private val projectService: ProjectService,
    private val projectsQueryService: ProjectsQueryService,
    private val projectCompletionService: ProjectCompletionService
) {
    open fun completeProject(project: ProjectId): DomainResult {
        return projectCompletionService.complete(project)
    }

    open fun reopenProject(projectId: ProjectId): DomainResult {
        return projectCompletionService.reopen(projectId)
    }

    open fun getProject(projectId: ProjectId): ProjectDto? {
        return projectsQueryService.findById(projectId)
    }

    open fun getAllProjects(): Page<ProjectDto> {
        return projectsQueryService.getAll()
    }

    open fun getOpenProjects(): Page<ProjectDto> {
        return projectsQueryService.getAllOpen()
    }

    open fun addProject(project: ProjectDto): ProjectDto {
        return ProjectDto.from(projectService.add(Project(name = project.name)))!!
    }

    open fun deleteProject(project: ProjectDto, forced: Boolean = false) {
        deleteProject(projectId = project.id, forced = forced)
    }

    fun deleteProject(projectId: ProjectId, forced: Boolean = false) {
        projectService.delete(projectId, forced = forced)
    }

    fun getInbox(): ProjectDto? {
        return ProjectDto.from(projectService.getInboxProject())
    }
}