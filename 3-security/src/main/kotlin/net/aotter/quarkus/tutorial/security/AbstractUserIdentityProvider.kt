package net.aotter.quarkus.tutorial.security

import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.repository.UserRepository
import javax.inject.Inject

abstract class AbstractUserIdentityProvider {
    @Inject
    lateinit var userRepository: UserRepository


    fun loadUserByUsername(username: String): Uni<User?> {
        return userRepository.findOneByDeletedIsFalseAndUsername(username)
    }

    fun buildSecurityIdentity(user: User): SecurityIdentity {
        val builder = QuarkusSecurityIdentity.builder()
            .setPrincipal{ user.username }
        builder.addRole(user.role.name)
        return builder.build() as SecurityIdentity
    }
}