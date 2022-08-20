package dev.bettercode.tasks.infra.adapter.rest

import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.tasks.TaskDto
import dev.bettercode.tasks.TasksFacade
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(TasksController::class)
@ContextConfiguration(classes = [TasksController::class])
internal class TasksControllerUnitTest {

    @MockBean
    private lateinit var projectsFacade: ProjectsFacade

    @MockBean
    private lateinit var tasksFacade: TasksFacade

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return empty list when INBOX not yet created`() {
        // given
        setupTasks(
            listOf()
        )

        // when
        mockMvc.perform(
            MockMvcRequestBuilders.get("/inbox/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            // then
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.jsonPath("$.content").isArray,
            MockMvcResultMatchers.jsonPath("$.content.length()").value(0)
        )
    }

    @Test
    fun `should return list of tickets from INBOX special project`() {
        // given
        setupTasks(
            listOf(
                TaskDto(name = "blog post #1"),
                TaskDto(name = "blog post #2"),
                TaskDto(name = "read chapter #1 of book ABC"),
                TaskDto(name = "read chapter #2 of book ABC")
            )
        )

        // when
        mockMvc.perform(
            MockMvcRequestBuilders.get("/inbox/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            // then
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.jsonPath("$.content").isArray,
            MockMvcResultMatchers.jsonPath("$.content.length()").value(4),
            MockMvcResultMatchers.jsonPath("$.content[0].name").value("blog post #1"),
            MockMvcResultMatchers.jsonPath("$.content[1].name").value("blog post #2"),
            MockMvcResultMatchers.jsonPath("$.content[2].name").value("read chapter #1 of book ABC"),
            MockMvcResultMatchers.jsonPath("$.content[3].name").value("read chapter #2 of book ABC")
        )
    }

    @Test
    fun `should support paging for list of INBOX tasks`() {
        // given
        setupTasksWithPaging(
            listOf(
                TaskDto(name = "blog post #1"),
                TaskDto(name = "blog post #2"),
                TaskDto(name = "read chapter #1 of book ABC"),
                TaskDto(name = "read chapter #2 of book ABC")
            ),
            pageSize = 2
        )

        // when - asking for first page
        mockMvc.perform(
            MockMvcRequestBuilders.get("/inbox/tasks?page=0&size=2")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            // then
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.jsonPath("$.content").isArray,
            MockMvcResultMatchers.jsonPath("$.content.length()").value(2),
            MockMvcResultMatchers.jsonPath("$.content[0].name").value("blog post #1"),
            MockMvcResultMatchers.jsonPath("$.content[1].name").value("blog post #2"),
            MockMvcResultMatchers.jsonPath("$.number").value(0),
            MockMvcResultMatchers.jsonPath("$.totalElements").value(4),
            MockMvcResultMatchers.jsonPath("$.totalPages").value(2)

        )

        // when - asking for second page
        mockMvc.perform(
            MockMvcRequestBuilders.get("/inbox/tasks?page=1&size=2")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            // then
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.jsonPath("$.content").isArray,
            MockMvcResultMatchers.jsonPath("$.content.length()").value(2),
            MockMvcResultMatchers.jsonPath("$.content[0].name").value("read chapter #1 of book ABC"),
            MockMvcResultMatchers.jsonPath("$.content[1].name").value("read chapter #2 of book ABC"),
            MockMvcResultMatchers.jsonPath("$.number").value(1),
            MockMvcResultMatchers.jsonPath("$.totalElements").value(4),
            MockMvcResultMatchers.jsonPath("$.totalPages").value(2)
        )
    }

    private fun setupTasks(tasks: List<TaskDto>) {
        doReturn(PageImpl(tasks)).`when`(tasksFacade).getOpenInboxTasks(any())
    }

    private fun setupTasksWithPaging(tasks: List<TaskDto>, pageSize: Int) {
        tasks.chunked(pageSize).forEachIndexed { pageNo, tasksForPage ->
            doReturn(PageImpl(tasksForPage, PageRequest.of(pageNo, pageSize), tasks.size.toLong())).`when`(tasksFacade)
                .getOpenInboxTasks(eq(PageRequest.of(pageNo, pageSize) as Pageable))
        }

    }

}