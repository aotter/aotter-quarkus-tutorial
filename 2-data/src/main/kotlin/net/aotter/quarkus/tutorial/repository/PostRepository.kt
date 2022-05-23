package net.aotter.quarkus.tutorial.repository

import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.po.Post
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostRepository: AuditingRepository<Post>(){
    suspend fun findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(
        authorId: ObjectId?, category: String?, published: Boolean?,
        page: Long, show: Int
    ): PageData<Post> {
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            authorId?.let {
                put(Post::authorId.name, it)
            }
            category?.let {
                put(Post::category.name, it)
            }
            published?.let {
                put(Post::published.name, it)
            }
        }
        return pageDataByCriteria(
            criteria = criteria,
            sort = Sort.by("lastModifiedTime", Sort.Direction.Descending),
            page = page,
            show = show
        )
    }

    suspend fun findOneByDeletedIsFalseAndIdAndPublished(
        id: ObjectId, published: Boolean?
    ): Post?{
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            put(Post::id.name, id)
            published?.let {
                put(Post::published.name, it)
            }
        }
        return  findByCriteria(criteria).firstResult().awaitSuspending()
    }
}
