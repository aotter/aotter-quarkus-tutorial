package security

import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.vertx.core.Vertx
import model.po.User
import repository.UserRepository
import javax.inject.Inject

abstract class AbstractMongoIdentityProvider {

    @Inject
    open lateinit var vertx: Vertx

    @Inject
    open lateinit var userRepository: UserRepository

    open fun buildSecurityIdentity(user: User): SecurityIdentity{
        val builder = QuarkusSecurityIdentity.builder()
            .setPrincipal{ user._id.toString() }
        user.roles?.forEach{ builder.addRole(it.name) }
        return builder.build() as SecurityIdentity
    }

}