package net.aotter.quarkus.tutorial.service

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoEntity
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
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

    fun getExistedPostSummary(authorIdValue: String?, category: String?, published: Boolean?, page: Long, show: Int): Uni<PageData<PostSummary>> {
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
            sort = Sort.by("title", Sort.Direction.Ascending),
            page = page,
            show = show
        ).map{ it.map(this::toPostSummary) }
    }

    fun getExistedPostDetail(id: String, published: Boolean?): Uni<PostDetail>{
        postRepository.count("id", ObjectId(id))
            .subscribe().with{ println(it) }


        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            put("id", ObjectId(id))
            published?.let {
                put("published", published)
            }
        }

        return postRepository.findByCriteria(criteria).firstResult()
            .onItem()
            .transform {
                if(it == null){
                    null
                }else{
                    toPostDetail(it)
                }
            }
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