package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.service.PostService
import net.aotter.quarkus.tutorial.util.abbreviate
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestPath
import javax.inject.Inject
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
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

    @Inject
    lateinit var postService: PostService

    @Inject
    lateinit var logger: Logger

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun posts(metaData: HTMLMetaData, pageData: PageData<PostSummary>): TemplateInstance
        @JvmStatic
        external fun postDetail(metaData: HTMLMetaData, postDetail: PostDetail): TemplateInstance
    }

    @GET
    fun listPosts(
        @QueryParam("category") category: String?,
        @QueryParam("authorId") authorId: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("6") show: Int
    ): Uni<TemplateInstance> {
        val metaData = buildHTMLMetaData(
            title = "BLOG",
            type = "website",
            description = "BLOG 有許多好文章"
        )
        return postService.getExistedPostSummary(authorId, category, true, page, show)
            .map{ pageData -> Templates.posts(metaData, pageData)  }
    }

    @Path("/posts/{postId}")
    @GET
    fun showPostDetail(
        @PathParam("postId") postId: String
    ): Uni<TemplateInstance> {
        val metaData = buildHTMLMetaData(
            title = "BLOG-Test title 1",
            type = "article",
            description = "Test content 1"
        )
        logger.info("postId: $postId")


        return postService.getExistedPostDetail(postId, true)
            .map { Templates.postDetail(metaData, it) }

    }

    private fun buildHTMLMetaData(title: String, type: String, description: String, image: String = ""): HTMLMetaData{
        val url = uriInfo.baseUriBuilder.replaceQuery("").toTemplate()
        return HTMLMetaData(title, type, description.abbreviate(20), url, image)
    }
}