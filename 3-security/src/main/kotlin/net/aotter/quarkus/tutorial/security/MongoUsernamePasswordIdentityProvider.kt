package net.aotter.quarkus.tutorial.security

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MongoUsernamePasswordIdentityProvider: AbstractUserIdentityProvider(), IdentityProvider<UsernamePasswordAuthenticationRequest> {
    override fun getRequestType(): Class<UsernamePasswordAuthenticationRequest> {
        return UsernamePasswordAuthenticationRequest::class.java
    }

    override fun authenticate(
        request: UsernamePasswordAuthenticationRequest?,
        context: AuthenticationRequestContext?
    ): Uni<SecurityIdentity> {
        val username = request?.username
        val password = request?.password?.password
        if(username == null || password == null){
            throw AuthenticationFailedException()
        }
        return loadUserByUsername(username).map { user ->
                user?.takeIf { BcryptUtil.matches(String(password), user?.credentials) }
                    ?.let { buildSecurityIdentity(it) }
                    ?: throw AuthenticationFailedException()
        }
    }
}