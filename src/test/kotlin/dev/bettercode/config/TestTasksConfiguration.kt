package dev.bettercode.config

import dev.bettercode.tasks.TasksFacade
import dev.bettercode.tasks.application.projects.ProjectAssignmentService
import dev.bettercode.tasks.application.projects.ProjectCompletionService
import dev.bettercode.tasks.application.projects.ProjectService
import dev.bettercode.tasks.application.tasks.ProjectDeletedHandler
import dev.bettercode.tasks.application.tasks.TaskCompletionService
import dev.bettercode.tasks.application.tasks.TaskService
import dev.bettercode.tasks.infra.adapter.db.inmemory.InMemoryProjectRepository
import dev.bettercode.tasks.infra.adapter.db.inmemory.InMemoryProjectsQueryRepository
import dev.bettercode.tasks.infra.adapter.db.inmemory.InMemoryQueryRepository
import dev.bettercode.tasks.infra.adapter.db.inmemory.InMemoryTasksRepository
import dev.bettercode.tasks.query.ProjectsQueryService
import dev.bettercode.tasks.query.TasksQueryService
import dev.bettercode.tasks.shared.InMemoryEventPublisher

class TestTasksConfiguration {
    companion object {
        private val taskRepo = InMemoryTasksRepository()
        private val projectRepo = InMemoryProjectRepository()
        private val tasksQueryRepository = InMemoryQueryRepository(taskRepo)
        private val projectsQueryRepository = InMemoryProjectsQueryRepository(projectRepo)
        fun tasksFacade(inMemoryEventPublisher: InMemoryEventPublisher = InMemoryEventPublisher()): TasksFacade {
            val projectService = ProjectService(projectRepo, inMemoryEventPublisher)
            val projectsQueryService = ProjectsQueryService(projectsQueryRepository)
            return TasksFacade(
                TaskService(taskRepo, projectRepo, projectService, inMemoryEventPublisher),
                TaskCompletionService(taskRepo, inMemoryEventPublisher),
                projectService,
                ProjectAssignmentService(projectRepo, taskRepo, inMemoryEventPublisher),
                ProjectCompletionService(projectRepo, inMemoryEventPublisher),
                TasksQueryService(tasksQueryRepository, taskRepo),
                projectsQueryService
            )
        }

        internal fun projectDeletedHandler(inMemoryEventPublisher: InMemoryEventPublisher = InMemoryEventPublisher()): ProjectDeletedHandler {
            return ProjectDeletedHandler(
                taskRepo,
                TasksQueryService(tasksQueryRepository, taskRepo),
                ProjectService(projectRepo, inMemoryEventPublisher),
                ProjectAssignmentService(
                    projectRepo, taskRepo, inMemoryEventPublisher
                )
            )
        }

        fun resetAll() {
            taskRepo.reset()
            projectRepo.reset()
        }
    }
}