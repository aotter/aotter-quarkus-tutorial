package net.aotter.quarkus.tutorial.repository

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheQuery
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.po.AuditingEntity
import java.util.stream.Stream

abstract class AuditingRepository<Entity: AuditingEntity>: ReactivePanacheMongoRepository<Entity>{

    override fun persist(entity: Entity): Uni<Entity> {
        entity.beforePersistOrUpdate()
        return super.persist(entity)
    }

    override fun persist(firstEntity: Entity, vararg entities: Entity): Uni<Void> {
        firstEntity.beforePersistOrUpdate()
        entities.forEach { it.beforePersistOrUpdate() }
        return super.persist(firstEntity, *entities)
    }

    override fun persist(entities: Stream<Entity>): Uni<Void> {
        entities.forEach{ it.beforePersistOrUpdate() }
        return super.persist(entities)
    }

    override fun persist(entities: Iterable<Entity>): Uni<Void> {
        entities.forEach { it.beforePersistOrUpdate() }
        return super.persist(entities)
    }

    override fun update(entity: Entity): Uni<Entity> {
        entity.beforePersistOrUpdate()
        return super.update(entity)
    }

    override fun update(firstEntity: Entity, vararg entities: Entity): Uni<Void> {
        firstEntity.beforePersistOrUpdate()
        entities.forEach { it.beforePersistOrUpdate() }
        return super.update(firstEntity, *entities)
    }

    override fun update(entities: Stream<Entity>): Uni<Void> {
        entities.forEach { it.beforePersistOrUpdate() }
        return super.update(entities)
    }

    override fun update(entities: Iterable<Entity>): Uni<Void> {
        entities.forEach { it.beforePersistOrUpdate() }
        return super.update(entities)
    }


    override fun persistOrUpdate(entity: Entity): Uni<Entity> {
        entity.beforePersistOrUpdate()
        return super.persistOrUpdate(entity)
    }

    override fun persistOrUpdate(firstEntity: Entity, vararg entities: Entity): Uni<Void> {
        firstEntity.beforePersistOrUpdate()
        entities.forEach { it.beforePersistOrUpdate() }
        return super.persistOrUpdate(firstEntity, *entities)
    }

    override fun persistOrUpdate(entities: Stream<Entity>): Uni<Void> {
        entities.forEach { it.beforePersistOrUpdate() }
        return super.persistOrUpdate(entities)
    }

    override fun persistOrUpdate(entities: Iterable<Entity>): Uni<Void> {
        entities.forEach { it.beforePersistOrUpdate() }
        return super.persistOrUpdate(entities)
    }

    suspend fun countByCriteria(criteria: Map<String, Any>): Long =
        if(criteria.isEmpty()){
            count().awaitSuspending()
        } else {
            count(buildQuery(criteria), criteria).awaitSuspending()
        }

    fun findByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("_id", Sort.Direction.Ascending)): ReactivePanacheQuery<Entity> =
        if(criteria.isEmpty()){
            findAll(sort)
        } else {
            find(buildQuery(criteria), sort, criteria)
        }

    suspend fun pageDataByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("_id", Sort.Direction.Ascending), page: Long, show: Int): PageData<Entity>{
        val total = countByCriteria(criteria)
        val list = findByCriteria(criteria, sort)
            .page(page.toInt() - 1, show)
            .list().awaitSuspending()
        return PageData(list, page, show, total)
    }

    private fun buildQuery(criteria: Map<String, Any>): String = criteria.keys.joinToString(separator = " and ") { """$it = :$it""" }
}