package net.aotter.quarkus.tutorial.service

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
import javax.inject.Named

@Named("AdminPostManageService")
@ApplicationScoped
class AdminPostManageService: PostManageService {
    @Inject
    lateinit var postRepository: PostRepository

    override suspend fun getSelfPostSummary(
        username: String,
        category: String?,
        authorIdValue: String?,
        page: Long,
        show: Int
    ): PageData<PostSummary> {
        val authorId = kotlin.runCatching {
            ObjectId(authorIdValue)
        }.getOrNull()

        return postRepository.findPageDataByDeletedIsFalseAndAuthorNameAndAuthorIdAndCategory(
            authorName = null,
            authorId = authorId,
            category = category,
            page = page,
            show = show
        ).map { this.toPostSummary(it) }
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
}