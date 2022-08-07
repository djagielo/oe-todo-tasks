package dev.bettercode.projects.infra.adapter.rest

import dev.bettercode.projects.ProjectDto
import dev.bettercode.projects.ProjectsFacade
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(ProjectsController::class)
@ContextConfiguration(classes = [ProjectsController::class])
class ProjectsControllerUnitTest {

    @AfterEach
    fun afterEach() {
        Mockito.reset(projectsFacade)
    }

    @MockBean
    private lateinit var projectsFacade: ProjectsFacade

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return list of projects`() {
        // given
        setupProjects(
            listOf(
                ProjectDto(name = "BLOG"),
                ProjectDto(name = "WORK")
            )
        )

        // when
        mockMvc.perform(
            MockMvcRequestBuilders.get("/projects")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            // then
            status().isOk,
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.content").isArray,
            jsonPath("$.content.length()").value(2),
            jsonPath("$.content[0].name").value("BLOG"),
            jsonPath("$.content[1].name").value("WORK")
        )
    }

    @Test
    fun `should return 204 when project gets deleted`() {
        // given
        setupProjects(
            listOf(
                ProjectDto(name = "BLOG"),
                ProjectDto(name = "WORK")
            )
        )

        // when
        mockMvc.perform(
            MockMvcRequestBuilders.get("/projects")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            // then
            status().isOk,
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.content").isArray,
            jsonPath("$.content.length()").value(2),
            jsonPath("$.content[0].name").value("BLOG"),
            jsonPath("$.content[1].name").value("WORK")
        )
    }

    private fun setupProjects(projects: List<ProjectDto>) {
        doReturn(PageImpl(projects)).`when`(projectsFacade).getOpenProjects()
    }
}