package net.aotter.quarkus.tutorial.repository

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheQuery
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.po.Post
import java.time.Instant
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostRepository: ReactivePanacheMongoRepository<Post>{

    fun persistOrUpdateWithAuditing(posts: Iterable<Post>): Uni<Void>{
        posts.forEach{ it.lastModifiedTime = it.lastModifiedTime?.let { Instant.now() } ?: it.createdTime }
        return persistOrUpdate(posts)
    }

    fun persistOrUpdateWithAuditing(post: Post): Uni<Post> {
        post.lastModifiedTime = post.lastModifiedTime?.let { Instant.now() } ?: post.createdTime
        return persistOrUpdate(post)
    }

    fun countByCriteria(criteria: Map<String, Any>): Uni<Long> =
        if(criteria.isEmpty())
            count()
        else
            count(buildQuery(criteria), criteria)

    fun findByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("id")): ReactivePanacheQuery<Post> =
        if(criteria.isEmpty())
            findAll(sort)
        else
            find(buildQuery(criteria), criteria)

    fun pageDataByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("id"), page: Long, show: Int): Uni<PageData<Post>>{
        val total = countByCriteria(criteria)
        val list = findByCriteria(criteria, sort).page(page.toInt() - 1, show).list()
        return Uni.combine().all().unis(total, list).asTuple()
            .map { PageData(it.item2, page, show, it.item1) }
    }

    fun buildQuery(criteria: Map<String, Any>): String = criteria.keys.joinToString(separator = " and ") { """$it = :$it""" }
}
