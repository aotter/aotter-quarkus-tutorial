package article

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateExtension
import io.quarkus.qute.TemplateInstance
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("article-list")
@TemplateExtension
class ArticleListResource {

    @Inject
    lateinit var articleRepository: ArticleRepository

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun articleList(articleList: List<ArticleView>?): TemplateInstance?
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    fun getAllArticleList(): TemplateInstance? {
//        val articleList = articleRepository.findAsList()
//        println("===============================")
//        println("articleList=$articleList")
//        println("===============================")

        val articleList: MutableList<ArticleView> = ArrayList()
        articleList.add(ArticleView(
            id = "622ff971a9aff08e2b0d7e25",
            category =  "分類一",
            title =  "測試標題一",
            content = "測試內容一",
            author = "914",
            lastModifiedTime = "2022/03/12"))

        articleList.add(ArticleView(
            id = "622ffab0a9aff08e2b0d7ea0",
            category =  "分類三",
            title =  "測試標題二",
            content = "測試內容二測試內容二",
            author = "915",
            lastModifiedTime = "2022/03/14"))

        articleList.add(ArticleView(
            id = "622ffab0a9aff08e2b0d7ea0",
            category =  "分類三",
            title =  "測試標題三",
            content = "測試內容三測試內容三測試內容三",
            author = "916",
            lastModifiedTime = "2022/03/14"))

        articleList.add(ArticleView(
            id = "622ffab0a9aff08e2b0d7ea0",
            category =  "分類二",
            title =  "測試標題四",
            content = "測試內容四測試內容四//測試內容四測試內容四//",
            author = "917",
            lastModifiedTime = "2022/03/14"))


        articleList.add(ArticleView(
            id = "622ffab0a9aff08e2b0d7ea0",
            category =  "分類一",
            title =  "測試標題五",
            content = "測試內容五～測試內容五～測試內容五～測試內容五～測試內容五～",
            author = "918",
            lastModifiedTime = "2022/03/14"))


        articleList.add(ArticleView(
            id = "622ffab0a9aff08e2b0d7ea0",
            category =  "分類四",
            title =  "測試標題六",
            content = "測試內容六！測試內容六！測試內容六！測試內容六！測試內容六！測試內容六！",
            author = "919",
            lastModifiedTime = "2022/03/14"))

        articleList.add(ArticleView(
            id = "622ffab0a9aff08e2b0d7ea0",
            category =  "分類三",
            title =  "測試標題七",
            content = "測試內容七測試內容七，測試內容七測試內容七測試內容七。測試內容七測試內容七",
            author = "920",
            lastModifiedTime = "2022/03/14"))

        return Templates.articleList(articleList)
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