package dev.bettercode.projects.infra.adapter.db.jdbc

import dev.bettercode.projects.domain.Project

internal class ProjectEntityMapper {
    fun toEntity(project: Project): ProjectEntity {
        return ProjectEntity(id = project.id.uuid, name = project.name)
    }
}