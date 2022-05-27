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
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.core.UriInfo

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource: AbstractTemplateResource() {
    @Inject
    lateinit var postService: PostService

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun posts(metaData: HTMLMetaData, securityContext: SecurityContext, pageData: PageData<PostSummary>): TemplateInstance
        @JvmStatic
        external fun postDetail(metaData: HTMLMetaData, securityContext: SecurityContext, postDetail: PostDetail): TemplateInstance
    }

    @GET
    suspend fun listPosts(
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
        val pageData = postService.getExistedPostSummary(authorId, category, true, page, show)
        return Templates.posts(metaData, securityContext, pageData)
    }

    @Path("/posts/{postId}")
    @GET
    suspend fun showPostDetail(
        @PathParam("postId") postId: String
    ): TemplateInstance {
        val postDetail = postService.getExistedPostDetail(postId, true)
        val metaData = buildHTMLMetaData(
            title = """"BLOG-${postDetail.title}""",
            type = "article",
            description = postDetail.content ?: ""
        )
        return Templates.postDetail(metaData, securityContext, postDetail)
    }
}