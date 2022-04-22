package model.po

import com.fasterxml.jackson.annotation.JsonIgnore
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

    var role: Role? = Role.USER

) : BaseMongoEntity<User>() {

    fun verifyPassword(passwordToVerify: CharArray): Boolean = runCatching {
        val factory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT)
        ModularCrypt.decode(password)
                .let { factory.translate(it) as BCryptPassword }
                .let { factory.verify(it, passwordToVerify) }
    }.getOrDefault(false)

}
