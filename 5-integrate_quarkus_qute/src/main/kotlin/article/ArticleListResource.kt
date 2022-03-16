package article

import com.mongodb.client.model.Filters
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import org.bson.conversions.Bson
import repository.ArticleRepository
import repository.ArticleRepository.ArticleView
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotlin.math.ceil


@Consumes(MediaType.TEXT_HTML)
@Produces(MediaType.TEXT_HTML)
@Path("/")
class ArticleListResource {

    @Inject
    lateinit var articleRepository: ArticleRepository

    private val PAGE_ENTITY_NUM = 6

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun articleList(articleList: List<ArticleView>?, totalPage: Int, author: String?): TemplateInstance
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
            articleRepository.findArticleListViaPage(filters,page?:1),totalPage,author)
    }
}