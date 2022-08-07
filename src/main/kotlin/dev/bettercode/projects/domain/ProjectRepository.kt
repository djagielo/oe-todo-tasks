package dev.bettercode.projects.domain

import dev.bettercode.projects.ProjectId

internal interface ProjectRepository {
    fun add(project: Project): Project
    fun get(projectId: ProjectId): Project?
    fun getInboxProject(): Project?
    fun save(project: Project)
    fun delete(projectId: ProjectId)
    fun createInbox(): Project
}