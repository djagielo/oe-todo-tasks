package dev.bettercode.config

import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.projects.application.ProjectCompletionService
import dev.bettercode.projects.application.ProjectService
import dev.bettercode.projects.infra.adapter.inmemory.InMemoryProjectRepository
import dev.bettercode.projects.infra.adapter.inmemory.InMemoryProjectsQueryRepository
import dev.bettercode.tasks.TasksFacade
import dev.bettercode.tasks.application.ProjectAssignmentService
import dev.bettercode.tasks.application.ProjectDeletedHandler
import dev.bettercode.tasks.application.TaskCompletionService
import dev.bettercode.tasks.application.TaskService
import dev.bettercode.tasks.infra.adapter.db.inmemory.InMemoryTasksQueryRepository
import dev.bettercode.tasks.infra.adapter.db.inmemory.InMemoryTasksRepository
import dev.bettercode.tasks.query.ProjectsQueryService
import dev.bettercode.tasks.query.TasksQueryService
import dev.bettercode.tasks.shared.InMemoryEventPublisher

class TestConfiguration {
    companion object {
        private val taskRepo = InMemoryTasksRepository()
        private val projectRepo = InMemoryProjectRepository()
        private val tasksQueryRepository = InMemoryTasksQueryRepository(taskRepo)
        private val inMemoryEventPublisher: InMemoryEventPublisher = InMemoryEventPublisher()
        fun tasksFacade(eventPublisher: InMemoryEventPublisher = inMemoryEventPublisher, projectsFacade: ProjectsFacade = projectsFacade(eventPublisher)): TasksFacade {
            val projectService = ProjectService(projectRepo, inMemoryEventPublisher)
            return TasksFacade(
                taskService = TaskService(taskRepo, projectRepo, projectService, eventPublisher),
                taskCompletionService = TaskCompletionService(taskRepo, eventPublisher),
                projectAssignmentService = ProjectAssignmentService(projectRepo, taskRepo, eventPublisher),
                tasksQueryService = TasksQueryService(tasksQueryRepository, taskRepo),
                projectsFacade = projectsFacade
            )
        }

        internal fun projectsFacade(
            eventPublisher: InMemoryEventPublisher = inMemoryEventPublisher
        ): ProjectsFacade {
            return ProjectsFacade(
                projectCompletionService = ProjectCompletionService(
                    eventPublisher = eventPublisher,
                    projectRepository = projectRepo
                ),
                projectService = ProjectService(
                    projectRepository = projectRepo,
                    eventPublisher = eventPublisher
                ),
                projectsQueryService = ProjectsQueryService(InMemoryProjectsQueryRepository(db = projectRepo))
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