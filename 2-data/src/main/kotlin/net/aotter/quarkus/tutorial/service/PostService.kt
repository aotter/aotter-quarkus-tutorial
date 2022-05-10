package net.aotter.quarkus.tutorial.service

import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import org.bson.types.ObjectId
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.collections.HashMap

@ApplicationScoped
class PostService {
    @Inject
    lateinit var postRepository: PostRepository

    fun getExistedPostPageData(authorIdValue: String?, category: String?, published: Boolean? , page: Long, show: Int): Uni<PageData<PostSummary>> {
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
        ).map { it.map(this::toPostSummary) }
    }

    private fun toPostSummary(post: Post): PostSummary = PostSummary(
        post.id.toString(),
        post.title ?: "",
        post.category ?: "",
        post.authorName ?: "",
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Taipei"))
            .withLocale(Locale.TAIWAN)
            .format(post.lastModifiedTime),
        post.published ?: true
    )
}