package security

import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.vertx.core.Vertx
import model.po.User
import repository.UserRepository
import javax.inject.Inject

abstract class AbstractMongoIdentityProvider {

    @Inject
    lateinit var vertx: Vertx

    @Inject
    lateinit var userRepository: UserRepository

    fun buildSecurityIdentity(user: User): SecurityIdentity{
        val builder = QuarkusSecurityIdentity.builder()
                .setPrincipal{ user.username }
        user.roles?.forEach{ builder.addRole(it.name) }
        return builder.build() as SecurityIdentity
    }

}