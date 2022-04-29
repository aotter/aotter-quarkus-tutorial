package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import net.aotter.quarkus.tutorial.util.abbreviate
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
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
        external fun posts(metaData: HTMLMetaData): TemplateInstance
        @JvmStatic
        external fun postDetail(metaData: HTMLMetaData): TemplateInstance
    }

    @GET
    fun listPosts(): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG",
            type = "website",
            description = "BLOG 有許多好文章"
        )
        return Templates.posts(metaData)
    }

    @Path("/posts/{postId}")
    @GET
    fun showPostDetail(): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG-Test title 1",
            type = "article",
            description = "Test content 1"
        )
        return Templates.postDetail(metaData)
    }

    private fun buildHTMLMetaData(title: String, type: String, description: String, image: String = ""): HTMLMetaData{
        val url = uriInfo.baseUriBuilder
            .path(uriInfo.requestUri.path.toString())
            .build().toString()

        return HTMLMetaData(title, type, description.abbreviate(20), url, image)
    }
}