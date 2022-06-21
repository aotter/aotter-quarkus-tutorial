package net.aotter.quarkus.tutorial.service

import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.exception.BusinessException
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

@Named("UserPostManageService")
@ApplicationScoped
class UserPostMangeService: PostManageService {
    @Inject
    lateinit var postRepository: PostRepository

    override suspend fun getSelfPostSummary(
        username: String,
        category: String?,
        authorName: String?,
        page: Long,
        show: Int
    ): PageData<PostSummary> {
        return postRepository.findPageDataByDeletedIsFalseAndAuthorNameAndCategory(
            authorName = username,
            category = category,
            page = page,
            show = show
        ).map { this.toPostSummary(it) }
    }

    override suspend fun publishSelfPost(username: String, idValue: String, status: Boolean) {
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw BusinessException("無此文章")
        val post = postRepository.findById(id).awaitSuspending() ?: throw BusinessException("無此文章")
        if(post.deleted || post.authorName != username){
            throw BusinessException("無此文章")
        }
        post.published = status
        postRepository.update(post).awaitSuspending()
    }

    override suspend fun deleteSelfPost(username: String, idValue: String) {
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw BusinessException("無此文章")
        val post = postRepository.findById(id).awaitSuspending() ?: throw BusinessException("無此文章")
        if(post.deleted || post.authorName != username){
            throw BusinessException("無此文章")
        }
        post.deleted = true
        postRepository.update(post).awaitSuspending()
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