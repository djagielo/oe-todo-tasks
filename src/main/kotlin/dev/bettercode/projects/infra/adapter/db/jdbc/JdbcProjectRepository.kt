package dev.bettercode.projects.infra.adapter.db.jdbc

import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.domain.Inbox
import dev.bettercode.projects.domain.Project
import dev.bettercode.projects.domain.ProjectRepository
import io.vavr.control.Try
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.util.*

internal class JdbcProjectRepository(private val jdbcTemplate: JdbcTemplate) : ProjectRepository {
    override fun add(project: Project): Project {
        jdbcTemplate.update(
            "INSERT INTO projects (id, name) VALUES(?, ?)",
            project.id.uuid.toString(), project.name
        )
        return project
    }

    override fun get(projectId: ProjectId): Project? {
        return jdbcTemplate.queryForObject(
            "select id, name from projects p where p.id=?",
            mapRowToProject(),
            projectId.uuid.toString()
        )
    }

    private fun mapRowToProject() = RowMapper { rs, _ ->
        Project(name = rs.getString("name"), id = ProjectId(UUID.fromString(rs.getString("id"))))
    }

    override fun getInboxProject(): Project? {
        return Try.of {
            jdbcTemplate.queryForObject(
                "select p.id,name from projects p join inboxes i on p.id=i.project_id where i.tenantId=0",
                mapRowToProject()
            )
        }.recover(EmptyResultDataAccessException::class.java) { null }.get()
    }

    override fun save(project: Project) {
        jdbcTemplate.update(
            "update projects set name=?, completion_date=? where id=?",
            project.name,
            project.completionDate,
            project.id.uuid.toString()
        )
    }

    override fun delete(projectId: ProjectId) {
        jdbcTemplate.update("delete from projects where id = ?", projectId.uuid.toString())
    }

    override fun createInbox(): Inbox {
        Inbox().let {
            add(it)
            jdbcTemplate.update("insert into inboxes (project_id) values (?);", it.id.uuid.toString())
            return it
        }
    }
}