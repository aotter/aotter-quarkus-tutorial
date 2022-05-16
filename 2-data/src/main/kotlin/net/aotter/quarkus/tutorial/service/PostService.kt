package net.aotter.quarkus.tutorial.service

import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.NotFoundException
import kotlin.collections.HashMap

@ApplicationScoped
class PostService {
    @Inject
    lateinit var postRepository: PostRepository
    @Inject
    lateinit var logger: Logger

    suspend fun getExistedPostSummary(authorIdValue: String?, category: String?, published: Boolean?, page: Long, show: Int): PageData<PostSummary> {
        val authorId = kotlin.runCatching {
            ObjectId(authorIdValue)
        }.getOrNull()

        return postRepository.findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(authorId, category, published, page, show)
            .map(this::toPostSummary)
    }

    suspend fun getExistedPostDetail(idValue: String, published: Boolean?): PostDetail{
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw NotFoundException("post detail not found")

        return postRepository.findOneByDeletedIsFalseAndIdAndPublished(id, published)
            ?.let(this::toPostDetail)
            ?: throw NotFoundException("post detail not found")
    }

    private fun toPostSummary(post: Post): PostSummary = PostSummary(
        id = post?.id.toString(),
        title = post.title ,
        category = post.category ,
        authorName = post.authorName,
        lastModifiedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Taipei"))
            .withLocale(Locale.TAIWAN)
            .format(post.lastModifiedTime),
        published = post.published
    )

    private fun toPostDetail(post: Post): PostDetail = PostDetail(
        category = post.category,
        title =  post.title,
        content = post.content,
        authorId = post.authorId.toString(),
        authorName = post.authorName,
        lastModifiedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Taipei"))
            .withLocale(Locale.TAIWAN)
            .format(post.lastModifiedTime)
    )
}