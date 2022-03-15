package article

import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/article")
class ArticleResource {
    @Inject
    lateinit var article: Template

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    fun getArticle(@QueryParam("articleId") articleId: String?): TemplateInstance {


        return article.data(
            "title", "測試標題一",
            "content","Qute is a templating engine designed specifically to meet the Quarkus needs. The usage of reflection is minimized to reduce the size of native images. The API combines both the imperative and the non-blocking reactive style of coding. In the development mode, all files located in src/main/resources/templates are watched for changes and modifications are immediately visible. Furthermore, we try to detect most of the template problems at build time. In this guide, you will learn how to easily render templates in your application.",
            "lastModifiedTime", "2022/03/12",
            "author", "914"
        )
    }
}