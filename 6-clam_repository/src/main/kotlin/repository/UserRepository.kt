package repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import model.po.User
import javax.inject.Singleton

@Singleton
class UserRepository: BaseMongoRepository<User>() {

    init {
        createIndexes(
                IndexModel(
                        Indexes.ascending(User::username.name),
                        IndexOptions().unique(true)
                )
        )
    }

    suspend fun findByUsername(username: String) = findOne(Filters.eq(User::username.name, username))

}