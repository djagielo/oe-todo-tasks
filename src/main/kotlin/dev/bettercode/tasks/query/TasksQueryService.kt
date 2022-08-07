package dev.bettercode.tasks.query

import dev.bettercode.projects.ProjectId
import dev.bettercode.tasks.TaskDto
import dev.bettercode.tasks.TaskId
import dev.bettercode.tasks.domain.TasksRepository
import dev.bettercode.tasks.infra.adapter.db.jdbc.TasksQueryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

internal class TasksQueryService(
    private val tasksQueryRepository: TasksQueryRepository,
    private val tasksRepository: TasksRepository
) {

    fun findById(taskId: TaskId): TaskDto? {
        return tasksRepository.get(taskId).let {
            TaskDto.from(it)
        }
    }

    fun findAllOpen(pageable: Pageable): Page<TaskDto> {
        return tasksQueryRepository.findAllOpen(pageable).map {
            TaskDto.from(it)
        }
    }

    fun findAllOpenForProject(pageable: Pageable, projectId: ProjectId): Page<TaskDto> {
        return tasksQueryRepository.findAllOpenForProject(pageable, projectId.uuid).map {
            TaskDto.from(it)
        }
    }

    fun findAllCompleted(pageable: Pageable): Page<TaskDto> {
        return tasksQueryRepository.findAllCompleted(pageable).map {
            TaskDto.from(it)
        }
    }

    fun findAllForProject(pageable: Pageable, projectId: ProjectId): Page<TaskDto> {
        return this.tasksQueryRepository.findAllByProjectId(pageable, projectId.uuid).map {
            TaskDto.from(it)
        }
    }

    fun findAllWithoutDueDate(pageable: Pageable): Page<TaskDto> {
        return this.tasksQueryRepository.findAllNoDueDate(pageable).map {
            TaskDto.from(it)
        }
    }

    fun findAllWithDueDate(pageable: Pageable, dueDate: LocalDate): Page<TaskDto> {
        return this.tasksQueryRepository.findAllWithDueDate(pageable, dueDate).map {
            TaskDto.from(it)
        }
    }
}