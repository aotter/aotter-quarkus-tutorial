package resource

import com.mongodb.client.model.Filters
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import org.bson.conversions.Bson
import repository.ArticleRepository
import repository.ArticleRepository.ArticleView
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotlin.math.ceil

@Consumes(MediaType.TEXT_HTML)
@Produces(MediaType.TEXT_HTML)
@Path("/")
class ArticleResource {

    @Inject
    lateinit var articleRepository: ArticleRepository

    private val PAGE_ENTITY_NUM = 6

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun article(article: ArticleView): TemplateInstance

        @JvmStatic
        external fun articleList(articleList: List<ArticleView>?, totalPage: Int): TemplateInstance
    }

    @GET
    @Path("article-content")
    suspend fun article(@QueryParam("articleId") articleId: String): TemplateInstance {
        val result = articleRepository.getArticleById(articleId)

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.TAIWAN)
            .withZone(ZoneId.systemDefault())

        return Templates.article(ArticleView(
            title = result!!.title,
            content = result.content,
            lastModifiedTime = formatter.format(result.lastModifiedTime),
            author = result.author,
            authorName = result.authorName
        ))
    }

    @GET
    @Path("article-list")
    suspend fun getAllArticleList(@QueryParam("author") author: String?, @QueryParam("page") page: Int?): TemplateInstance {
        val filters = mutableListOf<Bson>()
        filters.add(Filters.eq("enabled", true))
        if(!author.isNullOrBlank()){
            filters.add(Filters.eq("author", author))
        }

        val totalPage = ceil((articleRepository.count(Filters.and(filters)).toDouble() / PAGE_ENTITY_NUM)).toInt()

        return  Templates.articleList(
            articleRepository.getArticleListByUser(author,page?:1),totalPage)
    }
}