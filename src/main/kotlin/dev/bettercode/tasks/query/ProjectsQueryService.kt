package dev.bettercode.tasks.query

import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.infra.adapter.db.jdbc.ProjectsQueryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

internal class ProjectsQueryService(private val projectsQueryRepository: ProjectsQueryRepository) {
    fun getAll(pageable: Pageable = PageRequest.of(0, 100)): Page<ProjectDto> {
        return projectsQueryRepository.findAll(pageable).map {
            ProjectDto.from(it)
        }
    }

    fun findById(projectId: ProjectId): ProjectDto? {
        return projectsQueryRepository.findById(projectId.uuid).let {
            ProjectDto.from(it)
        }
    }

    fun getAllOpen(pageable: Pageable = PageRequest.of(0, 100)): Page<ProjectDto> {
        return projectsQueryRepository.findAllOpen(pageable).map {
            ProjectDto.from(it)
        }
    }
}