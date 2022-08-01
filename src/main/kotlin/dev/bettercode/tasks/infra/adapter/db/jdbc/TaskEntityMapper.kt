package dev.bettercode.tasks.infra.adapter.db.jdbc

import dev.bettercode.tasks.domain.Task

internal class TaskEntityMapper {
    fun toEntity(task: Task): TaskEntity {
        return TaskEntity(id = task.id.uuid, name=task.name, projectId = task.projectId?.uuid, completionDate = task.completionDate)
    }
}