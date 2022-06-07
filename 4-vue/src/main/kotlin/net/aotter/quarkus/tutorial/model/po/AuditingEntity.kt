package net.aotter.quarkus.tutorial.model.po

import java.time.Instant

abstract class AuditingEntity {
    var lastModifiedTime: Instant? = null
    var createdTime: Instant = Instant.now()

    fun beforePersistOrUpdate() = (this.lastModifiedTime?.let { Instant.now() } ?: this.createdTime).also { this.lastModifiedTime = it }
}