package dev.bettercode.tasks

import dev.bettercode.config.TestTasksConfiguration
import dev.bettercode.tasks.application.projects.ProjectDeleted
import dev.bettercode.tasks.application.tasks.ProjectDeletedHandler
import dev.bettercode.tasks.shared.InMemoryEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable

class ProjectDeletionTests {
    private lateinit var tasksFacade: TasksFacade
    private lateinit var projectDeletedHandler: ProjectDeletedHandler
    private lateinit var inMemoryEventPublisher: InMemoryEventPublisher

    @BeforeEach
    fun setup() {
        inMemoryEventPublisher = InMemoryEventPublisher()
        tasksFacade = TestTasksConfiguration.tasksFacade(inMemoryEventPublisher)
        projectDeletedHandler = TestTasksConfiguration.projectDeletedHandler(inMemoryEventPublisher)
        TestTasksConfiguration.resetAll()
    }

    @Test
    fun `all tasks should be deleted when project deleted with forced flag`() {
        // given - a project
        val projectToDelete = tasksFacade.addProject(ProjectDto("PROJECT_TO_DELETE"))
        // and - 3 tasks in it
        val initialTasks = TasksFixtures.aNoOfTasks(3)
        initialTasks.forEach {
            tasksFacade.addToProject(it, projectToDelete)
        }
        assertThat(tasksFacade.getTasksForProject(Pageable.ofSize(10), projectToDelete)).hasSize(3)

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
        val projectToDelete = tasksFacade.addProject(ProjectDto("PROJECT_TO_DELETE"))
        // and - 3 tasks in it
        val initialTasks = TasksFixtures.aNoOfTasks(3)
        initialTasks.forEach {
            tasksFacade.addToProject(it, projectToDelete)
        }
        assertThat(tasksFacade.getTasksForProject(Pageable.ofSize(10), projectToDelete)).hasSize(3)

        // when
        projectDeletedHandler.handle(ProjectDeleted(projectToDelete.id, forced = false))

        // then
        val inboxTasks = tasksFacade.getOpenInboxTasks(Pageable.ofSize(10))
        assertThat(inboxTasks).hasSize(initialTasks.size)
        assertThat(inboxTasks.map { it.name }).containsExactlyInAnyOrderElementsOf(initialTasks.map { it.name }.toList())
    }
}