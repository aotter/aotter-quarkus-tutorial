package model.po

import com.fasterxml.jackson.annotation.JsonIgnore
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.mongodb.panache.common.MongoEntity
import org.wildfly.security.password.PasswordFactory
import org.wildfly.security.password.interfaces.BCryptPassword
import org.wildfly.security.password.util.ModularCrypt
import security.Role

@MongoEntity
data class User(
    var username: String? = null,

    @JsonIgnore
    var password: String? = null,

    var roles: MutableSet<Role>? = mutableSetOf(Role.USER)
): BaseMongoEntity<User>(){

    companion object {
        suspend fun create(username: String, password: String, roles: MutableSet<Role>): User =
            User(username.trim().toLowerCase(),BcryptUtil.bcryptHash(password),roles).coroutineSave()
    }

    suspend fun updateRole(roles: MutableSet<Role>): User = this.apply { this.roles = roles }.coroutineSave()

    suspend fun updatePassword(password: String): User =
            this.apply { this.password = BcryptUtil.bcryptHash(password) }.coroutineSave()

    fun verifyPassword(passwordToVerify: CharArray): Boolean = runCatching {
        val factory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT)
        ModularCrypt.decode(password)
                .let { factory.translate(it) as BCryptPassword }
                .let { factory.verify(it, passwordToVerify) }
    }.getOrDefault(false)

}
