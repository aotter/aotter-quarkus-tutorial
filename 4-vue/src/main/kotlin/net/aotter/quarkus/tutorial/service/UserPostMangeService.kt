package net.aotter.quarkus.tutorial.service

import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.adapter.PostAdapter
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.exception.BusinessException
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import net.aotter.quarkus.tutorial.repository.UserRepository
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.inject.Named

@Named("UserPostManageService")
@ApplicationScoped
class UserPostMangeService: PostManageService {
    @Inject
    lateinit var postRepository: PostRepository

    @Inject
    lateinit var postAdapter: PostAdapter

    @Inject
    lateinit var userRepository: UserRepository

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
        ).map { postAdapter.toPostSummary(it) }
    }

    override suspend fun getSelfPostDetail(username: String, idValue: String): PostDetail {
        val post = getSelfPost(idValue, username)
        return post.let { postAdapter.toPostDetail(it) }
    }

    override suspend fun createPost(username: String, category: String, title: String, content: String) {
        val user = userRepository.findOneByDeletedIsFalseAndUsername(username).awaitSuspending() ?: throw BusinessException("此帳號已不存在")
        val post = Post(
            authorId = user.id!!,
            authorName = user.username,
            title = title,
            category = category,
            content = content,
            published = false,
            deleted = false
        )
        postRepository.persist(post).awaitSuspending()
    }

    override suspend fun updateSelfPost(
        username: String,
        idValue: String,
        category: String,
        title: String,
        content: String
    ) {
        val post = getSelfPost(idValue, username)
        post.category = category
        post.title = title
        post.content = content
        postRepository.update(post).awaitSuspending()
    }

    override suspend fun publishSelfPost(username: String, idValue: String, status: Boolean) {
        val post = getSelfPost(idValue, username)
        post.published = status
        postRepository.update(post).awaitSuspending()
    }

    override suspend fun deleteSelfPost(username: String, idValue: String) {
       val post = getSelfPost(idValue, username)
        post.deleted = true
        postRepository.update(post).awaitSuspending()
    }

    private suspend fun getSelfPost(idValue: String, username: String): Post{
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw BusinessException("無此文章")
        val post = postRepository.findById(id).awaitSuspending() ?: throw BusinessException("無此文章")
        if(post.deleted || post.authorName != username){
            throw BusinessException("無此文章")
        }
        return post
    }

}