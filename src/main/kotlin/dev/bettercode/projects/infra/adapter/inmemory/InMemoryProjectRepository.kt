package dev.bettercode.projects.infra.adapter.inmemory

import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.domain.Inbox
import dev.bettercode.projects.domain.Project
import dev.bettercode.projects.domain.ProjectRepository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryProjectRepository : ProjectRepository {
    private val db = ConcurrentHashMap<UUID, Project>()
    override fun add(project: Project): Project {
        db[project.id.uuid] = project
        return project
    }

    override fun get(projectId: ProjectId): Project? {
        return db[projectId.uuid]
    }

    override fun getInboxProject(): Project? {
        return db.values.find {
            it.name == Inbox.NAME
        }
    }

    override fun save(project: Project) {
        db[project.id.uuid] = project
    }

    override fun delete(projectId: ProjectId) {
        db.remove(projectId.uuid)
    }

    fun getAll(): List<Project> {
        return ArrayList(db.values)
    }

    override fun createInbox(): Project {
        Project(name = "INBOX").let {
            db[it.id.uuid] = it
            return it
        }
    }

    fun reset() {
        db.clear()
    }
}