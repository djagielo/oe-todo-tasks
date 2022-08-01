package dev.bettercode.projects.infra.adapter.rest

import dev.bettercode.commons.paging.PageResult
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class ProjectsController(private val projectsFacade: ProjectsFacade) {
    @GetMapping("/projects")
    internal fun getAllProjects(): ResponseEntity<PageResult<ProjectDto>> {
        return ResponseEntity.ok(PageResult(projectsFacade.getProjects()))
    }

    @PostMapping("/projects")
    internal fun createProject(@RequestBody projectDto: ProjectDto) {
        projectsFacade.addProject(projectDto)
    }

    @GetMapping("/projects/{id}")
    internal fun getProject(@PathVariable id: UUID): ResponseEntity<ProjectDto> {
        return projectsFacade.getProject(ProjectId(id))?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    internal fun deleteProject(@PathVariable id: UUID) {
        projectsFacade.deleteProject(ProjectId(id))
    }

    @PutMapping("/projects/{id}/completed")
    internal fun completeProject(@PathVariable id: UUID, @RequestBody status: Boolean): ResponseEntity<Boolean> {
        val result = if (status) {
            projectsFacade.completeProject(ProjectId(id))
        } else {
            projectsFacade.reopenProject(ProjectId(id))
        }

        return if (result.successful) {
            ResponseEntity.ok(true)
        } else {
            ResponseEntity.badRequest().build()
        }
    }
}