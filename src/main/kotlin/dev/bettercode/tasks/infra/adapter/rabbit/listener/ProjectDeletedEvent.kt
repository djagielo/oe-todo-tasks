package dev.bettercode.tasks.infra.adapter.rabbit.listener

data class ProjectDeletedEvent(
    var projectId: String? = null,
    var forced: Boolean? = null
)