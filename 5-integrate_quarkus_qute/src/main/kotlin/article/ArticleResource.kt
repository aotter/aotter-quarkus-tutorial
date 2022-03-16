package article

import com.mongodb.client.model.Filters
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.bson.types.ObjectId
import repository.ArticleRepository
import java.time.ZoneId
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/article")
class ArticleResource {
    @Inject
    lateinit var article: Template

    @Inject
    lateinit var articleRepository: ArticleRepository

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    suspend fun getArticle(@QueryParam("articleId") articleId: String?): TemplateInstance? {
        val result = articleRepository.findOne(
            Filters.and(
                Filters.eq("enabled", true),
                Filters.eq("_id",ObjectId(articleId))))
        if(result != null){
            return article.data(
                "title", result.title,
                "content", result.content,
                "lastModifiedTime", result.lastModifiedTime!!.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime(),
                "author", result.author
            )
        }
        return null
    }
}