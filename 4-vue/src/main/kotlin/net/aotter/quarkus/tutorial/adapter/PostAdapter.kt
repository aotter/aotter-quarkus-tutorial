package net.aotter.quarkus.tutorial.adapter

import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostAdapter {
    fun toPostSummary(post: Post): PostSummary = PostSummary(
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

    fun toPostDetail(post: Post): PostDetail = PostDetail(
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