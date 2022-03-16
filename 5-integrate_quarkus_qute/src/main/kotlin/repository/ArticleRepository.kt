package repository

import model.po.Article
import javax.inject.Singleton

@Singleton
class ArticleRepository: BaseMongoRepository<Article>() {}