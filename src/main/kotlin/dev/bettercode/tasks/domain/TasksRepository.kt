package dev.bettercode.tasks.domain

import dev.bettercode.tasks.TaskId

internal interface TasksRepository {
    fun add(task: Task): Task
    fun get(id: TaskId): Task?
    fun save(task: Task): Task
    fun delete(id: TaskId)
}
