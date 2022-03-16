package article

import com.mongodb.client.model.Filters
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import org.bson.conversions.Bson
import repository.ArticleRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Consumes(MediaType.TEXT_HTML)
@Produces(MediaType.TEXT_HTML)
@Path("/")
class ArticleListResource {

    @Inject
    lateinit var articleRepository: ArticleRepository

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun articleList(articleList: List<ArticleView>?): TemplateInstance
    }

    @GET
    @Path("article-list")
    suspend fun getAllArticleList( @QueryParam("author") author: String?): TemplateInstance {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.TAIWAN)
            .withZone(ZoneId.systemDefault())

        val filters = mutableListOf<Bson>()
        filters.add(Filters.eq("enabled", true))
        if(author != null){
            filters.add(Filters.eq("author", author))
        }

        return Templates.articleList(articleRepository.findAsList(Filters.and(filters))
            .sortedByDescending { it.lastModifiedTime }
            .map {
            ArticleView(
                id = it.id.toString(),
                category =  it.category,
                title =  it.title,
                content = it.content,
                author = it.author,
                lastModifiedTime = formatter.format( it.lastModifiedTime )
            )
        })
    }

    data class ArticleView(
        val id: String? = null,
        val category: String? = null,
        val title: String? = null,
        val content: String? = null,
        val author: String? = null,
        val lastModifiedTime: String? = null,
    )

}