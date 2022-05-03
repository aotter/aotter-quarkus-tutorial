package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.vertx.core.http.HttpServerRequest
import net.aotter.quarkus.tutorial.model.dto.Page
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.util.abbreviate
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriInfo

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource {
    @Context
    lateinit var uriInfo: UriInfo

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun posts(metaData: HTMLMetaData, pageData: Page<PostSummary>): TemplateInstance
        @JvmStatic
        external fun postDetail(metaData: HTMLMetaData, postDetail: PostDetail): TemplateInstance
    }

    @GET
    fun listPosts(
        request: HttpServerRequest,

        @QueryParam("category") category: String?,
        @QueryParam("authorId") authorId: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("6") show: Int
    ): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG",
            type = "website",
            description = "BLOG 有許多好文章"
        )
        val postSummary = PostSummary(
            id = "123",
            title = "Test 1",
            category = "類別一",
            authorName = "user",
            lastModifiedTime = "2022-04-06 12:01:00",
            published = true
        )
        val pageData = Page(arrayListOf(postSummary, postSummary, postSummary, postSummary, postSummary, postSummary), page, show, 100)
        return Templates.posts(metaData, pageData)
    }

    @Path("/posts/{postId}")
    @GET
    fun showPostDetail(): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG-Test title 1",
            type = "article",
            description = "Test content 1"
        )
        val postDetail = PostDetail(
            category = "類別一",
            title = "Test 1",
            content = "test content",
            authorId = "user",
            authorName = "user",
            lastModifiedTime = "2022-04-06 12:01:00"
        )
        return Templates.postDetail(metaData, postDetail)
    }

    private fun buildHTMLMetaData(title: String, type: String, description: String, image: String = ""): HTMLMetaData{
        val url = uriInfo.baseUriBuilder.replaceQuery("").toTemplate()
        return HTMLMetaData(title, type, description.abbreviate(20), url, image)
    }
}