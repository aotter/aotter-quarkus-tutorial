package resource

import com.mongodb.client.model.Filters
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.quarkus.qute.runtime.TemplateProducer
import model.vo.ArticleResponse
import org.bson.types.ObjectId
import repository.ArticleRepository
import service.ArticleService
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
class PublicArticleResource {

    @Inject
    lateinit var articleRepository: ArticleRepository

    @Inject
    lateinit var articleService: ArticleService

    @Inject
    lateinit var templateProducer: TemplateProducer

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun article(article: ArticleResponse): TemplateInstance

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

        return Templates.article(ArticleResponse(
            title = result!!.title,
            content = result.content,
            lastModifiedTime = formatter.format(result.lastModifiedTime),
            author = result.author.toString(),
            authorName = result.authorName,
        ))
    }

    @GET
    @Path("/{var:article-list|}")
    suspend fun getAllArticleList(@QueryParam("author") author: String?,
                                  @QueryParam("category") category: String?,
                                  @QueryParam("page") @DefaultValue("1") page: Int,
                                  @QueryParam("show") @DefaultValue("6") show: Int
                                  ): TemplateInstance =
        Templates.articleList().apply {
            val result = articleService.findAsListResponse(toObjectOrNull(author), category,true, page, show)

            data("articleList", result.list)
            data("totalPage", result.totalPages)
        }

    private fun toObjectOrNull(id: String?): ObjectId?{
        return if(id != null){
            ObjectId(id)
        }else{
            null
        }
    }
}