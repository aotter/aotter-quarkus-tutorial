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
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            authorIdValue?.let {
                put(Post::authorId.name, ObjectId(it))
            }
            category?.let {
                put(Post::category.name, it)
            }
            published?.let {
                put(Post::published.name, published)
            }
        }
        return postRepository.pageDataByCriteria(
            criteria = criteria,
            sort = Sort.by("lastModifiedTime", Sort.Direction.Descending),
            page = page,
            show = show
        ).map(this::toPostSummary)
    }

    suspend fun getExistedPostDetail(id: String, published: Boolean?): PostDetail{
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)

            kotlin.runCatching {
                ObjectId(id)
            }.onSuccess {
                put(Post::id.name, it)
            }.onFailure {
                logger.info(it.message)
                throw NotFoundException("post detail not found")
            }

            published?.let {
                put("published", published)
            }
        }
        return postRepository.findByCriteria(criteria).firstResult().awaitSuspending()
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