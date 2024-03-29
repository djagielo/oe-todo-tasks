@file:Suppress("unused")

package dev.bettercode.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import dev.bettercode.projects.ProjectsFacade
import dev.bettercode.projects.application.ProjectCompletionService
import dev.bettercode.projects.application.ProjectService
import dev.bettercode.projects.domain.ProjectRepository
import dev.bettercode.tasks.application.*
import dev.bettercode.tasks.domain.TasksRepository
import dev.bettercode.tasks.infra.adapter.db.jdbc.JdbcTasksRepository
import dev.bettercode.tasks.infra.adapter.db.jdbc.TaskEntity
import dev.bettercode.tasks.infra.adapter.db.jdbc.TasksQueryRepository
import dev.bettercode.tasks.infra.adapter.events.ProjectsSpringEventsListener
import dev.bettercode.tasks.infra.adapter.rabbit.listener.RabbitProjectEventsListener
import dev.bettercode.tasks.query.ProjectsQueryService
import dev.bettercode.tasks.query.TasksQueryService
import dev.bettercode.tasks.shared.DomainEventPublisher
import dev.bettercode.tasks.shared.InMemoryEventPublisher
import dev.bettercode.tasks.shared.RabbitEventPublisher
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate


@Configuration
@EnableJpaRepositories("dev.bettercode.tasks.infra.adapter.db.jdbc")
@EntityScan(basePackageClasses = [TaskEntity::class])
@Import(TaskEntity::class)
class TasksConfiguration {

    @Bean
    internal fun taskRepository(jdbcTemplate: JdbcTemplate): TasksRepository {
        return JdbcTasksRepository(jdbcTemplate)
    }

    @Bean
    internal fun tasksFacade(
        tasksUseCase: TaskService,
        tasksCompletionService: TaskCompletionService,
        projectService: ProjectService,
        projectAssignmentService: ProjectAssignmentService,
        projectCompletionService: ProjectCompletionService,
        tasksQueryService: TasksQueryService,
        projectsQueryService: ProjectsQueryService,
        projectsFacade: ProjectsFacade
    ): TasksFacade {
        return TasksFacade(
            taskService = tasksUseCase,
            projectsFacade = projectsFacade,
            taskCompletionService = tasksCompletionService,
            projectAssignmentService = projectAssignmentService,
            tasksQueryService = tasksQueryService
        )
    }

    @Bean
    internal fun tasksCrudService(
        tasksRepository: TasksRepository,
        projectRepository: ProjectRepository,
        projectService: ProjectService,
        eventPublisher: DomainEventPublisher
    ): TaskService {
        return TaskService(tasksRepository, projectRepository, projectService, eventPublisher)
    }

    @Bean
    internal fun projectsAssignmentService(
        projectRepository: ProjectRepository,
        tasksRepository: TasksRepository
    ): ProjectAssignmentService {
        return ProjectAssignmentService(projectRepository, tasksRepository, InMemoryEventPublisher())
    }

    @Bean
    internal fun taskCompletionService(
        tasksRepository: TasksRepository,
        eventPublisher: DomainEventPublisher
    ): TaskCompletionService {
        return TaskCompletionService(tasksRepository, eventPublisher)
    }

    @Bean
    internal fun tasksQueryService(
        tasksQueryRepository: TasksQueryRepository,
        tasksRepository: TasksRepository
    ): TasksQueryService {
        return TasksQueryService(tasksQueryRepository, tasksRepository)
    }


    @Bean
    internal fun domainEventPublisher(
        eventPublisher: ApplicationEventPublisher,
        rabbitTemplate: RabbitTemplate
    ): DomainEventPublisher {
        return RabbitEventPublisher(rabbitTemplate = rabbitTemplate)
    }

    @Bean
    internal fun projectCompletedHandler(): ProjectCompletedHandler {
        return ProjectCompletedHandler()
    }

    @Bean
    internal fun projectDeletedHandler(
        tasksRepository: TasksRepository,
        tasksQueryService: TasksQueryService,
        projectService: ProjectService,
        projectAssignmentService: ProjectAssignmentService
    ): ProjectDeletedHandler {
        return ProjectDeletedHandler(tasksRepository, tasksQueryService, projectService, projectAssignmentService)
    }

    @Bean
    internal fun projectEventsHandler(
        projectDeletedHandler: ProjectDeletedHandler,
        projectCompletedHandler: ProjectCompletedHandler
    ): ProjectsSpringEventsListener {
        return ProjectsSpringEventsListener(
            projectDeletedHandler = projectDeletedHandler,
            projectCompletedHandler = projectCompletedHandler
        )
    }

    @Bean
    internal fun projectEventsRabbitHandler(
        projectDeletedHandler: ProjectDeletedHandler,
        projectCompletedHandler: ProjectCompletedHandler
    ): RabbitProjectEventsListener {
        return RabbitProjectEventsListener(projectDeletedHandler)
    }

    @Bean
    internal fun rabbitTemplate(connectionFactory: ConnectionFactory, mapper: ObjectMapper): RabbitTemplate {
        val jsonRabbitTemplate = RabbitTemplate(connectionFactory);
        jsonRabbitTemplate.messageConverter = Jackson2JsonMessageConverter(mapper)
        return jsonRabbitTemplate

    }

    companion object {
        const val RABBIT_LISTENER_FACTORY_NAME: String = "rabbitListenerFactory"
    }

    @Bean
    fun rabbitListenerFactory(
        configurer: SimpleRabbitListenerContainerFactoryConfigurer,
        connectionFactory: ConnectionFactory
    ): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory().apply {
            // Set up message converter
            setMessageConverter(Jackson2JsonMessageConverter())
        }

        configurer.configure(factory, connectionFactory)
        return factory
    }
}
