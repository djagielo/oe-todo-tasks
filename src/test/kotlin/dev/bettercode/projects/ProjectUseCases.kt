package dev.bettercode.projects

import dev.bettercode.commons.events.AuditLogCommand
import dev.bettercode.config.TestConfiguration
import dev.bettercode.projects.application.ProjectCompleted
import dev.bettercode.projects.application.ProjectDeleted
import dev.bettercode.projects.application.ProjectReopened
import dev.bettercode.projects.application.TaskAssignedToProject
import dev.bettercode.tasks.TasksFacade
import dev.bettercode.fixtures.TasksFixtures
import dev.bettercode.tasks.shared.InMemoryEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ProjectUseCases {

    private val inMemoryEventPublisher = InMemoryEventPublisher()
    private val tasksFacade: TasksFacade =
        TestConfiguration.tasksFacade(eventPublisher = inMemoryEventPublisher)
    private val projectsFacade: ProjectsFacade =
        TestConfiguration.projectsFacade(eventPublisher = inMemoryEventPublisher)

    @BeforeEach
    fun cleanUpEvents() {
        this.inMemoryEventPublisher.clear()
        TestConfiguration.resetAll()
    }

    @Test
    fun `create project`() {
        // given - empty system
        assertThat(projectsFacade.getProjects()).isEmpty()

        // when
        projectsFacade.addProject(ProjectDto("BLOG"))

        // then
        val projects = projectsFacade.getProjects()
        assertThat(projects).hasSize(1)
        assertThat(projects.map { it.name }).hasSameElementsAs(listOf("BLOG"))
    }

    @Test
    fun `delete project`() {
        // given - a project of name BLOG
        val aProject = projectsFacade.addProject(ProjectDto("BLOG"))
        assertThat(projectsFacade.getProjects()).hasSize(1)

        // when
        projectsFacade.deleteProject(aProject)

        // then
        assertThat(projectsFacade.getProjects()).hasSize(0)
    }

    @Test
    fun `tasks get assigned to project`() {
        // given - a project of name BLOG
        val blogProject = projectsFacade.addProject(ProjectDto("BLOG"))
        // and - it has no tasks
        assertThat(tasksFacade.getOpenTasksForProject(project = blogProject)).hasSize(0)
        // and - 5 tasks are created
        val tasks = TasksFixtures.aNoOfTasks(5)
        tasks.forEach { tasksFacade.add(it) }

        // when
        tasks.take(3)
            .forEach {
                assertThat(tasksFacade.assignToProject(it, blogProject).successful).isTrue()
            }

        // then
        val blogTasks = tasksFacade.getOpenTasksForProject(project = blogProject)
        assertThat(blogTasks).hasSize(3)
        assertThat(blogTasks.map { it.name }).hasSameElementsAs(tasks.take(3).map { it.name })
    }

    @Test
    fun `tasks get unassigned from a project`() {
        // given - a project of name BLOG
        val blogProject = projectsFacade.addProject(ProjectDto("BLOG"))
        // and - it has 5 tasks
        val tasks = TasksFixtures.aNoOfTasks(5)
        tasks.forEach { tasksFacade.addToProject(it, blogProject) }

        // when
        tasks.take(3)
            .forEach {
                assertThat(tasksFacade.assignToProject(it, projectsFacade.getInbox()!!).successful).isTrue()
            }

        // then
        val blogTasks = tasksFacade.getOpenTasksForProject(project = blogProject)
        assertThat(blogTasks).hasSize(2)
        assertThat(blogTasks.map { it.name }).hasSameElementsAs(tasks.drop(3).map { it.name })
    }

    @Test
    fun `tasks by default get created in INBOX project`() {
        // given - no projects
        assertThat((projectsFacade.getProjects())).isEmpty()
        // and 5 tasks created
        val tasks = TasksFixtures.aNoOfTasks(5)
        tasks.forEach { tasksFacade.add(it) }

        // when - asking for projects
        val projects = projectsFacade.getProjects()

        // then - it should have INBOX projects created
        assertThat(projects).hasSize(1)
        assertThat(projects.map { it.name }).containsExactly("INBOX")
        assertThat(
            tasksFacade.getOpenTasksForProject(
                project = projects.first()
            )
        ).containsExactlyInAnyOrder(*tasks.toTypedArray())
    }

    @Test
    fun `should switch task assignment from INBOX to new project`() {
        // given - a task in Inbox project
        val task = TasksFixtures.aNoOfTasks(1).first()
        tasksFacade.add(task)
        // and - new project
        val privProject = projectsFacade.addProject(ProjectDto(name = "PRIV"))

        // when - asking for projects
        tasksFacade.assignToProject(tasksFacade.getOpenInboxTasks().first(), privProject)

        // then - there should be total of 2 projects
        val projects = projectsFacade.getProjects()
        assertThat(projects).hasSize(2)
        assertThat(projects.map { it.name }).containsExactlyInAnyOrder("INBOX", "PRIV")

        // and - INBOX project should have 0 tasks
        assertThat(tasksFacade.getOpenTasksForProject(projectId = projectsFacade.getInbox()!!.id)).isEmpty()
        // and - PRIV project has 1 task
        assertThat(tasksFacade.getOpenTasksForProject(project = privProject)).hasSize(1)
        // and - assign event gets published
        assertThat(inMemoryEventPublisher.events).contains(
            TaskAssignedToProject(task.id, privProject.id)
        )

        assertThat(inMemoryEventPublisher.events.filterIsInstance<AuditLogCommand>().map {
            it.message
        }.toList()).containsAll(
            listOf(
                "Task with id=TaskId(uuid=${task.id.uuid}) has been assigned to project with id=ProjectId(uuid=${privProject.id.uuid})"
            )
        )
    }

    @Test
    fun `task assignment to non-existing project fails`() {
        // given - a task in Inbox project
        val task = TasksFixtures.aNoOfTasks(1).first()
        tasksFacade.add(task)

        // when - trying to assign task to non-existing project
        val result =
            tasksFacade.assignToProject(tasksFacade.getOpenInboxTasks().first(), ProjectId())

        // then - failure with proper reason should be returned
        assertThat(result.failure).isTrue
        assertThat(result.reason).isEqualTo("No project with given id")
    }

    @Test
    fun `task cannot be assigned to completed project`() {
        // given - a task in Inbox project
        val task = TasksFixtures.aNoOfTasks(1).first()
        tasksFacade.add(task)
        // and a project that is completed
        val project = projectsFacade.addProject(ProjectDto(name = "COMPLETED"))
        projectsFacade.completeProject(project.id)

        // when - trying to assign task to non-existing project
        val result = tasksFacade.assignToProject(tasksFacade.getOpenInboxTasks().first(), project)

        // then - failure with proper reason should be returned
        assertThat(result.failure).isTrue
        assertThat(result.reason).isEqualTo("Cannot assign to completed project")
    }

    @Test
    fun `completed task cannot be reassigned`() {
        // given - a task that's completed
        val task = TasksFixtures.aNoOfTasks(1).first()
        tasksFacade.add(task)
        tasksFacade.complete(task)
        // and a project
        val project = projectsFacade.addProject(ProjectDto("BLOG"))

        // when - trying to assign completed task to different project
        val result = tasksFacade.assignToProject(task, project)

        // then - failure with proper reason should be returned
        assertThat(result.failure).isTrue
        assertThat(result.reason).isEqualTo("Cannot assign completed task")
    }

    @Test
    fun `completed task needs to be reopened before reassignment`() {
        // given - a task that's completed
        val task = TasksFixtures.aNoOfTasks(1).first()
        tasksFacade.add(task)
        tasksFacade.complete(task)
        // and a project
        val project = projectsFacade.addProject(ProjectDto("BLOG"))
        // and a task gets reopened
        tasksFacade.reopenTask(task)

        // when - trying to assign reopened task to project
        val result = tasksFacade.assignToProject(task, project)

        // then - failure with proper reason should be returned
        assertThat(result.successful).isTrue
    }

    @Test
    fun `empty project can be completed`() {
        // given - an empty project to be completed
        val project = projectsFacade.addProject(ProjectDto("PROJECT TO BE COMPLETED"))

        // when - trying to complete it again
        val result = projectsFacade.completeProject(project.id)

        // then - should success and emit event
        assertThat(result.successful).isTrue
        assertThat(inMemoryEventPublisher.events).contains(
            ProjectCompleted(project.id)
        )

        assertThat(inMemoryEventPublisher.events.filterIsInstance<AuditLogCommand>().map {
            it.message
        }.toList()).containsAll(
            listOf(
                "Project with id=ProjectId(uuid=${project.id.uuid}) has been completed"
            )
        )
    }

    @Test
    fun `completed project can be reopened at any time`() {
        // given - an empty project to be completed
        val project = projectsFacade.addProject(ProjectDto("PROJECT TO BE REOPENED"))
        projectsFacade.completeProject(project.id)

        // when - trying to complete it again
        val result = projectsFacade.reopenProject(project.id)

        // then - should success and emit event
        assertThat(result.successful).isTrue
        assertThat(inMemoryEventPublisher.events).contains(
            ProjectCompleted(project.id),
            ProjectReopened(project.id),
        )

        assertThat(inMemoryEventPublisher.events.filterIsInstance<AuditLogCommand>().map {
            it.message
        }.toList()).containsAll(
            listOf(
                "Project with id=ProjectId(uuid=${project.id.uuid}) has been completed",
                "Project with id=ProjectId(uuid=${project.id.uuid}) has been reopened"
            )
        )
    }

    @Test
    fun `project that had been completed already, cannot be completed again`() {
        // given - a project that's completed
        val project = projectsFacade.addProject(ProjectDto("COMPLETED_PROJECT"))
        // and a task gets reopened
        projectsFacade.completeProject(project.id)

        // when - trying to complete it again
        val result = projectsFacade.completeProject(project.id)

        // then - failure with proper reason should be returned
        assertThat(result.successful).isFalse
        assertThat(result.reason).isEqualTo("Project is already completed")
    }


    @Test
    fun `project deletion with forced=true flag should propagate that flag to event`() {
        // given - a project
        val newProject = projectsFacade.addProject(ProjectDto("NEW_PROJECT"))
        val tasks = TasksFixtures.aNoOfTasks(10)
        tasks.forEach {
            tasksFacade.add(it)
            tasksFacade.addToProject(it, newProject)
        }

        // when
        projectsFacade.deleteProject(newProject, forced = true)

        // then - failure with proper reason should be returned
        assertThat(inMemoryEventPublisher.events).contains(
            ProjectDeleted(newProject.id, forced = true)
        )
    }

    @Test
    fun `project deletion without forced flag should propagate that flag to event`() {
        // given - a project
        val newProject = projectsFacade.addProject(ProjectDto("NEW_PROJECT"))
        val tasks = TasksFixtures.aNoOfTasks(10)
        tasks.forEach {
            tasksFacade.add(it)
            tasksFacade.addToProject(it, newProject)
        }

        // when
        projectsFacade.deleteProject(newProject, forced = false)


        // then - failure with proper reason should be returned
        assertThat(projectsFacade.getProject(newProject.id)).isNull()
        assertThat(inMemoryEventPublisher.events).contains(
            ProjectDeleted(newProject.id, forced = false)
        )
    }
}
