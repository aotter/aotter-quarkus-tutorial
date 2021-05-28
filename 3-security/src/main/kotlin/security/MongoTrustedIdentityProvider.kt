package security

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.TrustedAuthenticationRequest
import io.smallrye.mutiny.Uni
import util.uni
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MongoTrustedIdentityProvider: AbstractMongoIdentityProvider(), IdentityProvider<TrustedAuthenticationRequest> {
    override fun getRequestType(): Class<TrustedAuthenticationRequest> {
        return TrustedAuthenticationRequest::class.java
    }

    override fun authenticate(
            request: TrustedAuthenticationRequest?,
            context: AuthenticationRequestContext?
    ): Uni<SecurityIdentity> {
        return vertx.uni{
            request?.principal
                    ?.let { userRepository.findByUsername(it) }
                    ?.let { buildSecurityIdentity(it) } ?: throw AuthenticationFailedException()
        }
    }

}