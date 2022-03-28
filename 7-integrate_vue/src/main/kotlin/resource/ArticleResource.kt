//package resource
//
//import com.mongodb.client.model.Filters
//import io.quarkus.qute.CheckedTemplate
//import io.quarkus.qute.TemplateInstance
//import org.bson.conversions.Bson
//import repository.ArticleRepository
//import repository.ArticleRepository.ArticleView
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.time.format.FormatStyle
//import java.util.*
//import javax.inject.Inject
//import javax.ws.rs.*
//import javax.ws.rs.core.MediaType
//import kotlin.math.ceil
//
//@Consumes(MediaType.TEXT_HTML)
//@Produces(MediaType.TEXT_HTML)
//@Path("/")
//class ArticleResource {
//
//    @Inject
//    lateinit var articleRepository: ArticleRepository
//
//    @CheckedTemplate
//    object Templates {
//        @JvmStatic
//        external fun article(article: ArticleView): TemplateInstance
//
//        @JvmStatic
//        external fun articleList(articleList: List<ArticleView>?, totalPage: Int): TemplateInstance
//    }
//
//    @GET
//    @Path("article-content")
//    suspend fun article(@QueryParam("articleId") articleId: String): TemplateInstance {
//        val result = articleRepository.getArticleById(articleId)
//
//        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
//            .withLocale(Locale.TAIWAN)
//            .withZone(ZoneId.systemDefault())
//
//        return Templates.article(ArticleView(
//            title = result!!.title,
//            content = result.content,
//            lastModifiedTime = formatter.format(result.lastModifiedTime),
//            author = result.author,
//        ))
//    }
//
//    @GET
//    @Path("article-list")
//    suspend fun getAllArticleList(@QueryParam("author") author: String?, @QueryParam("page") page: Int?): TemplateInstance {
//        val query = if(!author.isNullOrBlank()){
//            mapOf("author" to author)
//        }else{
//            null
//        }
//
//        val list = articleRepository.getArticleListByQuery(query,page?:1)
//
//        val totalPage = if(list.isNotEmpty()){
//            list[0].pageLength!!
//        }else{
//            1
//        }
//
//        return  Templates.articleList(list,totalPage)
//    }
//}