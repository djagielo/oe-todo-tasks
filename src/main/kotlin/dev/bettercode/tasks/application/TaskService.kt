package dev.bettercode.tasks.application

import dev.bettercode.commons.events.AuditLogCommand
import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.application.ProjectService
import dev.bettercode.projects.domain.ProjectRepository
import dev.bettercode.tasks.TaskId
import dev.bettercode.tasks.domain.Task
import dev.bettercode.tasks.domain.TasksRepository
import dev.bettercode.tasks.shared.DomainEventPublisher
import dev.bettercode.tasks.shared.DomainResult

internal class TaskService(
    private val tasksRepository: TasksRepository,
    private val projectRepository: ProjectRepository,
    private val projectService: ProjectService,
    private val eventPublisher: DomainEventPublisher
) {
    fun add(task: Task): DomainResult {
        val result = task.assignTo(projectService.getInboxProject())
        return if (result.successful) {
            tasksRepository.add(task)
            eventPublisher.publish(TaskCreated(taskId = task.id))
            eventPublisher.publish(AuditLogCommand(message = "Task with id=TaskId(uuid=${task.id.uuid}) has been created"))
            DomainResult.success()
        } else {
            result
        }
    }

    fun addTaskForAProject(task: Task, projectId: ProjectId): Task? {
        return projectRepository.get(projectId)?.let {
            task.assignTo(it)
            return tasksRepository.save(task)
        }
    }

    fun delete(id: TaskId) {
        tasksRepository.delete(id)
    }
}