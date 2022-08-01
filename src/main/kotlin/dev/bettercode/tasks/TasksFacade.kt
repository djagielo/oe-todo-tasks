package dev.bettercode.tasks

import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.tasks.application.ProjectAssignmentService
import dev.bettercode.tasks.application.TaskCompletionService
import dev.bettercode.tasks.application.TaskService
import dev.bettercode.tasks.domain.Task
import dev.bettercode.tasks.query.TasksQueryService
import dev.bettercode.tasks.shared.DomainResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.Clock
import java.time.LocalDate

open class TasksFacade internal constructor(
    private val taskService: TaskService,
    private val projectsFacade: ProjectsFacade,
    private val taskCompletionService: TaskCompletionService,
    private val projectAssignmentService: ProjectAssignmentService,
    private val tasksQueryService: TasksQueryService,
) {
    fun add(task: TaskDto): DomainResult {
        val taskToAdd = Task(id = task.id, name = task.name)
        if (task.dueDate != null) {
            taskToAdd.dueTo(task.dueDate).apply {
                if (this.failure)
                    return this
            }
        }
        return taskService.add(taskToAdd)
    }

    fun delete(id: TaskId) {
        return taskService.delete(id)
    }

    fun complete(id: TaskId, clock: Clock = Clock.systemDefaultZone()): DomainResult {
        return taskCompletionService.complete(id, clock)
    }

    fun complete(task: TaskDto, clock: Clock = Clock.systemDefaultZone()): DomainResult {
        return this.complete(task.id, clock)
    }

    fun get(id: TaskId): TaskDto? {
        return tasksQueryService.findById(id)
    }

    fun getOpenInboxTasks(pageable: Pageable = PageRequest.of(0, 100)): Page<TaskDto> {
        return projectsFacade.getInbox()?.let {
            tasksQueryService.findAllOpenForProject(pageable, it.id)
        } ?: Page.empty()
    }

    fun getAllCompleted(pageable: Pageable = PageRequest.of(0, 100)): Page<TaskDto> {
        return tasksQueryService.findAllCompleted(pageable)
    }

    fun reopenTask(task: TaskDto, clock: Clock = Clock.systemDefaultZone()) {
        this.reopenTask(task.id, clock)
    }

    fun reopenTask(id: TaskId, clock: Clock = Clock.systemDefaultZone()) {
        taskCompletionService.reopen(id, clock)
    }

    fun assignToProject(task: TaskDto, project: ProjectDto): DomainResult {
        return projectAssignmentService.assign(task.id, project.id)
    }

    fun assignToProject(task: TaskDto, projectId: ProjectId): DomainResult {
        return projectAssignmentService.assign(task.id, projectId)
    }

    fun getTasksForProject(pageable: Pageable = PageRequest.of(0, 100), project: ProjectDto): Page<TaskDto> {
        return getTasksForProject(pageable, project.id)
    }

    fun getTasksForProject(pageable: Pageable = PageRequest.of(0, 100), projectId: ProjectId): Page<TaskDto> {
        return projectsFacade.getProject(projectId)?.let {
            return tasksQueryService.findAllForProject(pageable, projectId)
        } ?: Page.empty()
    }

    fun addToProject(task: TaskDto, project: ProjectDto): TaskDto {
        return TaskDto.from(
            taskService.addTaskForAProject(
                Task(name = task.name, id = task.id), project.id
            )
        )!!
    }

    fun getAllWithoutDueDate(pageable: Pageable = PageRequest.of(0, 100)): Page<TaskDto> {
        return tasksQueryService.findAllWithoutDueDate(pageable)
    }

    fun getTasksDueDate(pageable: Pageable = PageRequest.of(0, 100), dueDate: LocalDate): Page<TaskDto> {
        return tasksQueryService.findAllWithDueDate(pageable, dueDate)
    }

    open fun getAllOpen(pageable: Pageable): Page<TaskDto> {
        return tasksQueryService.findAllOpen(pageable)
    }
}