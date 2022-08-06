package dev.bettercode.tasks.integration

import dev.bettercode.componentTests.TasksServiceComponentTests
import dev.bettercode.fixtures.TasksFixtures
import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.tasks.TasksFacade
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = [TasksIntegrationTest.EnvInitializer::class])
class TasksIntegrationTest {

    @Autowired
    lateinit var tasksFacade: TasksFacade

    @Autowired
    lateinit var projectsFacade: ProjectsFacade

    class EnvInitializer :
        ApplicationContextInitializer<ConfigurableApplicationContext?> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=${db.jdbcUrl}",
            ).applyTo(applicationContext)
        }
    }

    @AfterEach
    fun afterEach() {
        tasksFacade.getAllOpen(Pageable.ofSize(100)).forEach {
            tasksFacade.delete(it.id)
        }
    }

    companion object {
        @Container
        private val db: MariaDBContainer<*> = TasksServiceComponentTests.KMariaDBContainer("mariadb:10.6")
            .withNetworkAliases("mariadb").withExposedPorts(3306).withEnv(
                mapOf(
                    "MARIADB_ROOT_PASSWORD" to "password"
                )
            )
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