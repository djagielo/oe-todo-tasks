package dev.bettercode.projects.application

import dev.bettercode.projects.ProjectId
import dev.bettercode.projects.domain.Project
import dev.bettercode.projects.domain.ProjectRepository
import dev.bettercode.tasks.shared.DomainEventPublisher

internal class ProjectService(
    private val projectRepository: ProjectRepository,
    private val eventPublisher: DomainEventPublisher
) {
    fun add(project: Project): Project {
        val result = projectRepository.add(project)
        eventPublisher.publish(ProjectCreated(result.id))
        return result
    }

    fun delete(projectId: ProjectId, forced: Boolean = false) {
        projectRepository.delete(projectId)
        eventPublisher.publish(ProjectDeleted(projectId, forced = forced))
    }

    fun getInboxProject(): Project {
        var inbox = projectRepository.getInboxProject()
        if (inbox == null) {
            inbox = projectRepository.createInbox()
            eventPublisher.publish(ProjectCreated(inbox.id))
        }
        return inbox
    }
}
