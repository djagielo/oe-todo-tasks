package dev.bettercode.tasks

import dev.bettercode.config.TestConfiguration
import dev.bettercode.fixtures.TasksFixtures
import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.projects.application.ProjectDeleted
import dev.bettercode.tasks.application.ProjectDeletedHandler
import dev.bettercode.tasks.shared.InMemoryEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable

class ProjectDeletionTests {
    private lateinit var tasksFacade: TasksFacade
    private lateinit var projectsFacade: ProjectsFacade
    private lateinit var projectDeletedHandler: ProjectDeletedHandler
    private lateinit var inMemoryEventPublisher: InMemoryEventPublisher

    @BeforeEach
    fun setup() {
        inMemoryEventPublisher = InMemoryEventPublisher()
        projectsFacade = TestConfiguration.projectsFacade()
        tasksFacade = TestConfiguration.tasksFacade(inMemoryEventPublisher, projectsFacade)
        projectDeletedHandler = TestConfiguration.projectDeletedHandler(inMemoryEventPublisher)
    }

    @Test
    fun `all tasks should be deleted when project deleted with forced flag`() {
        // given - a project
        val projectToDelete = projectsFacade.addProject(ProjectDto("PROJECT_TO_DELETE"))
        // and - 3 tasks in it
        val initialTasks = TasksFixtures.aNoOfTasks(3)
        initialTasks.forEach {
            tasksFacade.addToProject(it, projectToDelete)
        }
        assertThat(tasksFacade.getOpenTasksForProject(Pageable.ofSize(10), projectToDelete)).hasSize(3)

        // when
        projectDeletedHandler.handle(ProjectDeleted(projectToDelete.id, forced = true))

        // then
        initialTasks.forEach {
            assertThat(tasksFacade.get(it.id)).isNull()
        }
    }

    @Test
    fun `all tasks should be moved to inbox when project deleted without forced flag`() {
        // given - a project
        val projectToDelete = projectsFacade.addProject(ProjectDto("PROJECT_TO_DELETE"))
        // and - 3 tasks in it
        val initialTasks = TasksFixtures.aNoOfTasks(3)
        initialTasks.forEach {
            tasksFacade.addToProject(it, projectToDelete)
        }
        assertThat(tasksFacade.getOpenTasksForProject(Pageable.ofSize(10), projectToDelete)).hasSize(3)

        // when
        projectDeletedHandler.handle(ProjectDeleted(projectToDelete.id, forced = false))

        // then
        val inboxTasks = tasksFacade.getOpenInboxTasks(Pageable.ofSize(10))
        assertThat(inboxTasks).hasSize(initialTasks.size)
        assertThat(inboxTasks.map { it.name }).containsExactlyInAnyOrderElementsOf(initialTasks.map { it.name }
            .toList())
    }
}