package repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.quarkus.elytron.security.common.BcryptUtil
import model.po.User
import security.Role
import javax.inject.Singleton

@Singleton
class UserRepository : BaseMongoRepository<User>() {

    init {
        createIndexes(
                IndexModel(
                        Indexes.ascending(User::username.name),
                        IndexOptions().unique(true)
                )
        )
    }

    /**
     * create new user
     *
     * @param username email format username
     * @param password password
     * @param role  user role
     */
    suspend fun create(username: String, password: String, roles: MutableSet<Role>): User =
        save(User(username = username.trim().toLowerCase(), password = BcryptUtil.bcryptHash(password), roles = roles))

    suspend fun updateRole(user: User, roles: MutableSet<Role>): User = save(user.apply { this.roles = roles })

    suspend fun updatePassword(user: User,password: String): User =
        save(user.apply { this.password = BcryptUtil.bcryptHash(password) })

    suspend fun findByUsername(username: String) = findOne(Filters.eq(User::username.name, username))
}