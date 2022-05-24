package net.aotter.quarkus.tutorial.security

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.TrustedAuthenticationRequest
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MongoTrustedIdentityProvider: AbstractUserIdentityProvider(), IdentityProvider<TrustedAuthenticationRequest> {
    override fun getRequestType(): Class<TrustedAuthenticationRequest> {
        return TrustedAuthenticationRequest::class.java
    }

    override fun authenticate(
        request: TrustedAuthenticationRequest?,
        context: AuthenticationRequestContext?
    ): Uni<SecurityIdentity> {
        val username = request?.principal ?: throw AuthenticationFailedException()
        return loadUserByUsername(username).map { user ->
            user?.let { buildSecurityIdentity(it) }
                ?: throw AuthenticationFailedException()
        }
    }
}