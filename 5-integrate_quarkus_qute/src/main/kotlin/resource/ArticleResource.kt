package resource

import com.mongodb.client.model.Filters
import io.quarkus.qute.TemplateInstance
import io.quarkus.resteasy.reactive.qute.RestTemplate
import org.bson.types.ObjectId
import org.jboss.resteasy.reactive.server.core.CurrentRequestManager
import repository.ArticleRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/article")
class ArticleResource {
    @Inject
    lateinit var articleRepository: ArticleRepository

    @GET
    @Consumes(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN)
    // suspend fun may cause otherHttpContextObjec's value be null => 500 NPE
   suspend fun article(@QueryParam("articleId") articleId: String?): TemplateInstance {
        val result = articleRepository.findOne(
            Filters.and(
                Filters.eq("enabled", true),
                Filters.eq("_id",ObjectId(articleId))))

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.TAIWAN)
            .withZone(ZoneId.systemDefault())

        val articleView = ArticleRepository.ArticleView(
            title = result!!.title,
            content = result.content,
            lastModifiedTime = formatter.format(result.lastModifiedTime),
            author = result.author
        )
        return RestTemplate.data("myArticle", articleView)
    }
}