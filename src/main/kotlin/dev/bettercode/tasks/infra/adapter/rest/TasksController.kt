@file:Suppress("unused")

package dev.bettercode.tasks.infra.adapter.rest

import dev.bettercode.commons.paging.PageResult
import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.tasks.TaskDto
import dev.bettercode.tasks.TaskId
import dev.bettercode.tasks.TasksFacade
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
class TasksController(val tasksFacade: TasksFacade, val projectsFacade: ProjectsFacade) {
    val logger: Logger = LoggerFactory.getLogger(TasksController::class.java)

    @GetMapping("/tasks/{id}")
    internal fun getById(@PathVariable id: UUID): ResponseEntity<TaskDto> {
        return tasksFacade.get(TaskId(id))?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/tasks")
    internal fun getAllOpenTasks(
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("size", required = false) size: Int?
    ): ResponseEntity<PageResult<TaskDto>> {
        return ResponseEntity.ok(
            PageResult(
                tasksFacade.getAllOpen(PageRequest.of(page ?: 0, size ?: 100))
            )
        )
    }

    @PostMapping("/tasks")
    internal fun create(@RequestBody taskRequest: TaskRequest): ResponseEntity<TaskDto> {
        val taskDto = taskRequest.toTaskDto()
        val result = tasksFacade.add(taskDto)
        return if (result.successful) {
            ResponseEntity.created(URI.create("tasks/${taskDto.id.uuid}"))
                .body(taskDto)
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    internal fun delete(@PathVariable id: UUID) {
        tasksFacade.delete(TaskId(id))
    }

    @PutMapping("/tasks/{id}/status")
    internal fun complete(@PathVariable id: UUID, @RequestBody taskCompletionRequest: TaskCompletionRequest) {
        if (taskCompletionRequest.completed)
            tasksFacade.complete(TaskId(id))
        else
            tasksFacade.reopenTask(TaskId(id))
    }

    @GetMapping("/projects/{id}/tasks")
    internal fun getTasksForProject(@PathVariable id: UUID, pageable: Pageable): ResponseEntity<PageResult<TaskDto>> {
        return projectsFacade.getProject(ProjectId(id))?.let {
            ResponseEntity.ok(PageResult(tasksFacade.getOpenTasksForProject(pageable, it.id)))
        } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/projects/{id}/tasks")
    internal fun addTaskToProject(@PathVariable id: UUID, @RequestBody task: TaskRequest): ResponseEntity<TaskDto> {
        return projectsFacade.getProject(ProjectId(id))?.let { project ->
            ResponseEntity.ok(tasksFacade.addToProject(task.toTaskDto(), project))
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/inbox/tasks")
    internal fun getInboxTasks(pageable: Pageable): ResponseEntity<PageResult<TaskDto>> {
        return ResponseEntity.ok(
            PageResult(tasksFacade.getOpenInboxTasks(pageable))
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception) {
        logger.error(ex.message, ex)
    }
}