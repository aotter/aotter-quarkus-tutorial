package resource

import com.mongodb.client.model.Filters
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.quarkus.qute.runtime.TemplateProducer
import model.po.Article
import org.bson.conversions.Bson
import org.bson.types.ObjectId
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
class PublicArticleResource {

    @Inject
    lateinit var articleRepository: ArticleRepository

    @Inject
    lateinit var templateProducer: TemplateProducer

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun article(article: ArticleView): TemplateInstance

        @JvmStatic
        external fun articleList(): TemplateInstance
    }

    /**
     * get TemplateInstance with path(start in resources/templates/)
     */
    fun renderTemplate(templatePath: String): TemplateInstance? {
        return templateProducer.getInjectableTemplate(templatePath).instance()
    }

    @GET
    @Path("article-content")
    suspend fun article(@QueryParam("articleId") articleId: String): TemplateInstance {
        val result = articleRepository.findOne(Filters.eq("_id",ObjectId(articleId)))

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.TAIWAN)
            .withZone(ZoneId.systemDefault())

        return Templates.article(ArticleView(
            title = result!!.title,
            content = result.content,
            lastModifiedTime = formatter.format(result.lastModifiedTime),
            author = result.author.toString(),
            authorName = result.authorName,
        ))
    }

    @GET
    @Path("article-list")
    suspend fun getAllArticleList(@QueryParam("author") author: String?,
                                  @QueryParam("category") category: String?,
                                  @QueryParam("page") page: Int?): TemplateInstance =
        Templates.articleList().apply {
            val filters = mutableListOf<Bson>()
            filters.add(Filters.eq(Article::published.name, true))
            filters.add(Filters.eq(Article::visible.name, true))

            val list = when{
                author != null ->{
                    filters.add(Filters.eq(Article::author.name, ObjectId(author)))
                    articleRepository.findPublished(ObjectId(author),null,page?:1)
                }
                category != null ->{
                    filters.add(Filters.eq(Article::category.name, category))
                    articleRepository.findPublished(null,category,page?:1)
                }
                else -> articleRepository.findPublished(null,null,page?:1)
            }

            val totalPage = articleRepository.getPageLength(filters)

            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.TAIWAN)
                .withZone(ZoneId.systemDefault())

            val articleList = list.map {
                ArticleView(
                    id = it.id.toString(),
                    author = it.author.toString(),
                    authorName = it.authorName,
                    category = it.category,
                    title = it.title,
                    content = it.content,
                    lastModifiedTime = formatter.format(it.lastModifiedTime),
                )
            }

            data("articleList", articleList)
            data("totalPage", totalPage)
        }
}