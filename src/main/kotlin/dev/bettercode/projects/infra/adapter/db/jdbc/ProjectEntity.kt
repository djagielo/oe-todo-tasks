package dev.bettercode.projects.infra.adapter.db.jdbc

import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "projects")
class ProjectEntity(
    @Id
    @Type(type="uuid-char")
    val id: UUID = UUID.randomUUID(),
    val name: String = ""
)
