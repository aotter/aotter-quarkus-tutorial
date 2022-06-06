package net.aotter.quarkus.tutorial.util.template

import io.quarkus.qute.TemplateExtension
import javax.ws.rs.core.SecurityContext

@TemplateExtension
class SecurityExtensions {
    companion object{
        @JvmStatic
        fun isAuthenticated(securityContext: SecurityContext): Boolean{
            return securityContext.userPrincipal != null
        }
    }
}