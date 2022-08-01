package dev.bettercode.projects

import dev.bettercode.projects.application.ProjectCompletionService
import dev.bettercode.projects.application.ProjectService
import dev.bettercode.projects.domain.ProjectRepository
import dev.bettercode.projects.infra.adapter.db.jdbc.JdbcProjectRepository
import dev.bettercode.projects.infra.adapter.db.jdbc.ProjectEntity
import dev.bettercode.projects.infra.adapter.db.jdbc.ProjectsQueryRepository
import dev.bettercode.tasks.query.ProjectsQueryService
import dev.bettercode.tasks.shared.DomainEventPublisher
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate

@EnableJpaRepositories("dev.bettercode.projects.infra.adapter.db.jdbc")
@EntityScan(basePackageClasses = [ProjectEntity::class])
@Import(ProjectEntity::class)
class ProjectsConfiguration {
    @Bean
    internal fun projectsRepository(jdbcTemplate: JdbcTemplate): ProjectRepository {
        return JdbcProjectRepository(jdbcTemplate)
    }

    @Bean
    internal fun projectsCrudService(
        projectRepository: ProjectRepository,
        eventPublisher: DomainEventPublisher
    ): ProjectService {
        return ProjectService(projectRepository, eventPublisher)
    }

    @Bean
    internal fun projectCompletionService(
        projectRepository: ProjectRepository,
        eventPublisher: DomainEventPublisher
    ): ProjectCompletionService {
        return ProjectCompletionService(projectRepository, eventPublisher)
    }

    @Bean
    internal fun projectsQueryService(projectsQueryRepository: ProjectsQueryRepository): ProjectsQueryService {
        return ProjectsQueryService(projectsQueryRepository)
    }

    @Bean
    internal fun projectsFacade(
        projectsService: ProjectService,
        projectsQueryService: ProjectsQueryService,
        projectCompletionService: ProjectCompletionService
    ): ProjectsFacade {
        return ProjectsFacade(
            projectService = projectsService,
            projectsQueryService = projectsQueryService,
            projectCompletionService = projectCompletionService
        )
    }
}