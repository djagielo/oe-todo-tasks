package dev.bettercode.tasks.integration

import dev.bettercode.fixtures.TasksFixtures
import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.shared.IntegrationTestBase
import dev.bettercode.tasks.TasksFacade
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container


@SpringBootTest
class TasksIntegrationTests: IntegrationTestBase() {

    @Autowired
    lateinit var tasksFacade: TasksFacade

    @Autowired
    lateinit var projectsFacade: ProjectsFacade

    companion object {
        @Container
        val rabbit: RabbitMQContainer = rabbitMQContainer()
        @DynamicPropertySource
        @JvmStatic
        fun configure(registry: DynamicPropertyRegistry) {
            registry.add("spring.rabbitmq.host", rabbit::getHost)
            registry.add("spring.rabbitmq.port", rabbit::getAmqpPort)
        }
    }


    @AfterEach
    fun afterEach() {
        tasksFacade.getAllOpen(Pageable.ofSize(100)).forEach {
            tasksFacade.delete(it.id)
        }

        projectsFacade.getAllProjects().forEach {
            projectsFacade.deleteProject(it.id, forced = true)
        }
    }

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
        assertThat(tasksFacade.getOpenTasksForProject(Pageable.ofSize(10), blogProject)).hasSize(3)

        // when - completing the task
        assertThat(tasksFacade.complete(tasks.first().id).successful).isTrue

        // then - the task should be marked as completed and completion date should be set
        assertThat(tasksFacade.getOpenTasksForProject(Pageable.ofSize(10), blogProject)).hasSize(2)
        assertThat(
            tasksFacade.getOpenTasksForProject(Pageable.ofSize(10), blogProject).map { it.name }).hasSameElementsAs(
            listOf(tasks[1].name, tasks[2].name)
        )
    }

    @Test
    @Disabled
    fun `should move tasks for a given project to INBOX when it's deleted with force flag off`() {
        // given - a saved task
        val blogTasks = TasksFixtures.aNoOfTasks(3)
        val inboxTasks = TasksFixtures.aNoOfTasks(2)
        inboxTasks.forEach { tasksFacade.add(it) }
        val blogProject = projectsFacade.addProject(ProjectDto(name = "BLOG"))
        blogTasks.forEach { task ->
            tasksFacade.addToProject(task, blogProject)
        }
        assertThat(tasksFacade.getOpenTasksForProject(Pageable.ofSize(10), blogProject)).hasSize(3)

        // when - completing the task
        projectsFacade.deleteProject(blogProject, forced = false)

        // then - the task should be marked as completed and completion date should be set
        assertThat(tasksFacade.getOpenInboxTasks(Pageable.ofSize(10))).hasSize(
            blogTasks.size + inboxTasks.size
        )
    }

    @Test
    fun `should delete tasks for a given project when it's deleted with force flag on`() {
        // given - a saved task
        val blogTasks = TasksFixtures.aNoOfTasks(3)
        val inboxTasks = TasksFixtures.aNoOfTasks(2)
        inboxTasks.forEach { tasksFacade.add(it) }
        val blogProject = projectsFacade.addProject(ProjectDto(name = "BLOG"))
        blogTasks.forEach { task ->
            tasksFacade.addToProject(task, blogProject)
        }
        assertThat(tasksFacade.getOpenTasksForProject(Pageable.ofSize(10), blogProject)).hasSize(3)

        // when - completing the task
        projectsFacade.deleteProject(blogProject, forced = true)

        // then - the task should be marked as completed and completion date should be set
        assertThat(tasksFacade.getOpenInboxTasks(Pageable.ofSize(10))).hasSize(
            inboxTasks.size
        )
    }
}