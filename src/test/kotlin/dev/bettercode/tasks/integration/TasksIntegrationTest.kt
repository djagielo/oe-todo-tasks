package dev.bettercode.tasks.integration

import dev.bettercode.config.IntegrationTestsConfiguration
import dev.bettercode.fixtures.TasksFixtures
import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.tasks.TasksFacade
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

@SpringBootTest(classes = [IntegrationTestsConfiguration::class])
class TasksIntegrationTest {

    @Autowired
    lateinit var tasksFacade: TasksFacade

    @Autowired
    lateinit var projectsFacade: ProjectsFacade

    @Test
    fun `should create and get task`() {
        // given - a saved task
        val task = TasksFixtures.aNoOfTasks(1).first()
        tasksFacade.add(task)

        // when - getting all open tasks
        val tasks = tasksFacade.getAllOpen(PageRequest.of(0, 1))

        // then - the task should be marked as completed and completion date should be set
        assertThat(tasks).hasSize(1)
        assertThat(tasks.map { it.name }).containsExactlyInAnyOrder(task.name)
    }

    @Test
    fun `should create and complete task`() {
        // given - a saved task
        val tasks = TasksFixtures.aNoOfTasks(3)
        val blogProject = projectsFacade.addProject(ProjectDto(name = "BLOG"))
        tasks.forEach { task ->
            tasksFacade.addToProject(task, blogProject)
        }
        assertThat(tasksFacade.getTasksForProject(Pageable.ofSize(10), blogProject)).hasSize(3)

        // when - completing the task
        assertThat(tasksFacade.complete(tasks.first().id).successful).isTrue

        // then - the task should be marked as completed and completion date should be set
        assertThat(tasksFacade.getTasksForProject(Pageable.ofSize(10), blogProject)).hasSize(2)
        assertThat(tasksFacade.getTasksForProject(Pageable.ofSize(10), blogProject).map { it.name }).hasSameElementsAs(
            listOf(tasks[1].name, tasks[2].name)
        )

    }
}