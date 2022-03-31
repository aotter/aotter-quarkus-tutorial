package service

import model.po.Article
import model.vo.ArticleListResponse
import model.vo.BaseListResponse
import org.bson.types.ObjectId
import repository.ArticleRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Singleton

@Singleton
class ArticleService {

    @Inject
    lateinit var articleRepo: ArticleRepository

    suspend fun findAsListResponse(
        authorId: ObjectId?, category: String?, published: Boolean?, page: Int, show: Int): BaseListResponse<ArticleListResponse> {
        val total = articleRepo.count(authorId, category, published)
        val list = articleRepo.list(authorId, category, published, page, show)
        return BaseListResponse(convertToArticleListResponse(list), page, show, total)
    }

    private fun convertToArticleListResponse(list: List<Article>): List<ArticleListResponse>{
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.TAIWAN)
            .withZone(ZoneId.systemDefault())

        return list.map {
            ArticleListResponse(
                id = it.id.toString(),
                authorName = it.authorName,
                category = it.category,
                title = it.title,
                published = it.published,
                lastModifiedTime = formatter.format(it.lastModifiedTime),
            )
        }
    }
}