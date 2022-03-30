package repository

import com.mongodb.client.model.*
import io.quarkus.mongodb.FindOptions
import io.quarkus.panache.common.Page
import io.smallrye.mutiny.coroutines.awaitSuspending
import model.dto.ArticleReq
import model.po.Article
import model.vo.ArticleListResponse
import org.bson.conversions.Bson
import org.bson.types.ObjectId
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

    //TODO add publishedTime and different published Status
    /**
     * update article published status by id
     * @param id [ObjectId] of the article
     * @param published [Boolean] of the article
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
     * @param data [ArticleReq] of the article
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
     * delete article by id
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
    suspend fun findPublished(authorId: ObjectId?, category: String?, page: Int = 1, limit: Int) =
        find(authorId, category, true, page, limit)


    /**
     * @param authorId
     * @param category
     * @param published
     * @param page
     * @param show
     */
    suspend fun find(authorId: ObjectId?, category: String?, published: Boolean?, page: Int = 1, show: Int): List<Article> {

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

    //TODO move to service
    fun  convertToArticleReqList(pageLength: Int?, list: List<Article>, ): List<ArticleListResponse>{
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
                pageLength = pageLength
            )
        }
    }

    suspend fun getPageLength(filters: List<Bson>): Int{
        val totalArticlesNum = count(Filters.and(filters))
        return ceil(totalArticlesNum.toDouble() / 10).toInt()
    }
}