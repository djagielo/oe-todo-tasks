package dev.bettercode.projects

import dev.bettercode.projects.domain.Project
import dev.bettercode.projects.infra.adapter.db.jdbc.ProjectEntity
import java.util.*


data class ProjectDto(
    val id: ProjectId = ProjectId(),
    val name: String,
) {
    constructor(id: UUID, name: String): this(ProjectId(id), name)

    constructor(name: String) : this(ProjectId(), name)

    companion object {
        internal fun from(project: Project?): ProjectDto? {
            return project?.let { p -> ProjectDto(p.id, p.name) }
        }

        internal fun from(project: ProjectEntity?): ProjectDto? {
            return project?.let { p -> ProjectDto(p.id, p.name) }
        }
    }
}
