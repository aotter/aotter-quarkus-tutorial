package repository

import com.mongodb.client.model.*
import io.quarkus.mongodb.FindOptions
import io.quarkus.panache.common.Page
import io.smallrye.mutiny.coroutines.awaitSuspending
import model.dto.ArticleReq
import model.po.Article
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class ArticleRepository: BaseMongoRepository<Article>() {

    init {
        createIndexes(
            IndexModel(
                Indexes.compoundIndex(
                    Indexes.ascending(Article::category.name),
                    Indexes.ascending(Article::published.name),
                    Indexes.descending(Article::createdTime.name)
                ),
            ),
            IndexModel(
                Indexes.compoundIndex(
                    Indexes.ascending(Article::author.name),
                    Indexes.ascending(Article::published.name),
                    Indexes.descending(Article::createdTime.name)
                )
            )
        )
    }

    val PAGE_ENTITY_NUM = 10

    /**
     * publish article by id
     * @param id [ObjectId] of the article
     */
    suspend fun updatePublishStatus(id: ObjectId, published: Boolean): Article? {
        return col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::published.name, published),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    /**
     * update article by id
     * @param id [ObjectId] of the article
     * @Req data[ArticleReq] of the article
     */
    suspend fun update(id: ObjectId, data: ArticleReq): Article? {
        return col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::category.name, data.category),
                Updates.set(Article::title.name, data.title),
                Updates.set(Article::content.name, data.content),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    /**
     * update article by id
     * @param id [ObjectId] of the article
     */
    suspend fun delete(id: ObjectId): Article? {
        return col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::visible.name, false),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    /**
     * @param authorId
     * @param category
     * @param page
     * @param limit
     */
    suspend fun findPublished(authorId: ObjectId?, category: String?, page: Int = 1, limit: Int = PAGE_ENTITY_NUM) =
        find(authorId, category, true, page, limit)


    /**
     * @param authorId
     * @param category
     * @param published
     * @param page
     * @param show
     */
    suspend fun find(authorId: ObjectId?, category: String?, published: Boolean?, page: Int = 1, show: Int = PAGE_ENTITY_NUM): List<Article> {

        // reuse Page for logic check
        val checkedPage = Page.of(page - 1 , show)

        val filters = listOfNotNull(
            published?.let { Filters.eq(Article::published.name, it) },
            authorId?.let { Filters.eq(Article::author.name, it) },
            category?.let { Filters.eq(Article::category.name, it) },
            Filters.eq("visible", true)
        )
        val filter = if (filters.size > 1) {
            Filters.and(filters)
        } else {
            filters.firstOrNull()
        }

        val options = FindOptions()
            .sort(Sorts.descending(Article::createdTime.name))
            .limit(checkedPage.size)
            .skip((checkedPage.index) * checkedPage.size)

        val find = filter?.let { col.find(filter, options) } ?: col.find(options)

        return find.collect().asList().awaitSuspending()
    }

    suspend fun convertToArticleView(authorId: ObjectId?, category: String?, published: Boolean?, list: List<Article>, ): List<ArticleView>{

        val filters = listOfNotNull(
            published?.let { Filters.eq(Article::published.name, it) },
            authorId?.let { Filters.eq(Article::author.name, it) },
            category?.let { Filters.eq(Article::category.name, it) },
            Filters.eq("visible", true)
        )
        var totalPage = 1
        if (filters.size > 1) {
            Filters.and(filters)
            totalPage = getPageLength(filters.toMutableList())
        }

        val articleViewList = mutableListOf<ArticleView>()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.TAIWAN)
            .withZone(ZoneId.systemDefault())

        list.forEach { article ->
            val articleView = ArticleView(
                id = article.id.toString(),
                author = article.author.toString(),
                authorName = article.authorName,
                category = article.category,
                title = article.title,
                content = article.content,
                published = article.published,
                lastModifiedTime = formatter.format(article.lastModifiedTime),
                pageLength = totalPage
            )
            articleViewList.add(articleView)
        }

        return articleViewList
    }

    suspend fun getPageLength(filters: MutableList<Bson>): Int{
        val totalArticlesNum = count(Filters.and(filters))
        return if(totalArticlesNum > PAGE_ENTITY_NUM){
            ceil(totalArticlesNum.toDouble() / PAGE_ENTITY_NUM).toInt()
        }else{
            1
        }
    }

    data class ArticleView(
        val id: String? = null,
        val author:String? = null,
        val authorName:String? = null,
        val category: String? = null,
        val title: String? = null,
        val content: String? = null,
        val lastModifiedTime: String? = null,
        val published: Boolean?= null,
        val pageLength: Int? = 1
    )
}