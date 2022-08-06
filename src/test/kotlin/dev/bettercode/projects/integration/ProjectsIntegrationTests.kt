package dev.bettercode.projects.integration

import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.shared.MariaDbIntegrationTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ProjectsIntegrationTests: MariaDbIntegrationTestBase() {

    @Autowired
    private lateinit var projectsFacade: ProjectsFacade

    @AfterEach
    fun afterEach() {
        projectsFacade.getAllProjects().forEach {
            projectsFacade.deleteProject(it)
        }
    }

    @Test
    fun `create, get and then delete project`() {
        // given
        val blog = projectsFacade.addProject(ProjectDto(name = "BLOG"))
        projectsFacade.addProject(ProjectDto(name = "WORK"))

        // when
        val projects = projectsFacade.getAllProjects()
        // then
        assertThat(projects).hasSize(2)
        assertThat(projects.map { it.name }).hasSameElementsAs(listOf("WORK", "BLOG"))

        // when
        projectsFacade.deleteProject(blog)

        // then
        assertThat(projectsFacade.getAllProjects()).hasSize(1)
        assertThat(projectsFacade.getAllProjects().map { it.name }).hasSameElementsAs(listOf("WORK"))
    }

    @Test
    fun `should complete and reopen the project`() {
        // given
        val blog = projectsFacade.addProject(ProjectDto(name = "BLOG"))
        projectsFacade.addProject(ProjectDto(name = "WORK"))

        assertThat(projectsFacade.getOpenProjects()).hasSize(2)
        assertThat(projectsFacade.getOpenProjects().map { it.name }).hasSameElementsAs(listOf("WORK", "BLOG"))

        // when
        projectsFacade.completeProject(blog.id)

        // then
        assertThat(projectsFacade.getOpenProjects()).hasSize(1)
        assertThat(projectsFacade.getOpenProjects().map { it.name }).hasSameElementsAs(listOf("WORK"))

        // when
        projectsFacade.reopenProject(blog.id)
        assertThat(projectsFacade.getOpenProjects()).hasSize(2)
        assertThat(projectsFacade.getOpenProjects().map { it.name }).hasSameElementsAs(listOf("WORK", "BLOG"))
    }

}