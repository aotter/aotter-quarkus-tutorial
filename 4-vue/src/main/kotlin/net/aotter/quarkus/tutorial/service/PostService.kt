package net.aotter.quarkus.tutorial.service

import net.aotter.quarkus.tutorial.adapter.PostAdapter
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.NotFoundException

@ApplicationScoped
class PostService {
    @Inject
    lateinit var postRepository: PostRepository

    @Inject
    lateinit var postAdapter: PostAdapter

    suspend fun getExistedPostSummary(authorIdValue: String?, category: String?, published: Boolean?, page: Long, show: Int): PageData<PostSummary> {
        val authorId = kotlin.runCatching {
            ObjectId(authorIdValue)
        }.getOrNull()

        return postRepository.findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(authorId, category, published, page, show)
            .map(postAdapter::toPostSummary)
    }

    suspend fun getExistedPostDetail(idValue: String, published: Boolean?): PostDetail{
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw NotFoundException("post detail not found")

        return postRepository.findOneByDeletedIsFalseAndIdAndPublished(id, published)
            ?.let(postAdapter::toPostDetail)
            ?: throw NotFoundException("post detail not found")
    }

}