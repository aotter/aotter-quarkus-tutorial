package security

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest
import io.smallrye.mutiny.Uni
import util.uni

@ApplicationScoped
class MongoIdentityProvider: AbstractMongoIdentityProvider(), IdentityProvider<UsernamePasswordAuthenticationRequest> {
    override fun getRequestType(): Class<UsernamePasswordAuthenticationRequest> {
        return UsernamePasswordAuthenticationRequest::class.java
    }

    override fun authenticate(
            request: UsernamePasswordAuthenticationRequest?,
            context: AuthenticationRequestContext?
    ): Uni<SecurityIdentity> {
        return vertx.uni {
            request?.username?.trim()?.toLowerCase()
                    ?.let { userRepository.findByUsername(it) }
                    ?.takeIf { user -> request.password?.password?.let { user.verifyPassword(it) } ?: false }
                    ?.let { buildSecurityIdentity(it) }
                    ?: throw AuthenticationFailedException()
        }
    }
}