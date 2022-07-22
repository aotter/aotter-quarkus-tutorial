package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.*

@Path("/")
@Produces(MediaType.TEXT_HTML)
class UserResource: AbstractTemplateResource() {
    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    lateinit var cookieName: String

    @ConfigProperty(name = "quarkus.http.auth.form.location-cookie")
    lateinit var redirectCookieName: String

    @ConfigProperty(name = "quarkus.http.auth.form.login-page")
    lateinit var loginPage: String


    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun login(metaData: HTMLMetaData, securityContext: SecurityContext): TemplateInstance
        @JvmStatic
        external fun signup(metaData: HTMLMetaData, securityContext: SecurityContext): TemplateInstance
    }

    @GET
    @Path("/login")
    fun login(): TemplateInstance{
        val metaData = buildHTMLMetaData(
            title = "BLOG-登入",
            type = "website",
            description = "登入BLOG系統"
        )
        return Templates.login(metaData, securityContext)
    }

    @GET
    @Path("/signup")
    fun signup(): TemplateInstance{
        val metaData = buildHTMLMetaData(
            title = "BLOG-註冊會員",
            type = "website",
            description = "註冊BLOG系統會員"
        )
        return Templates.signup(metaData, securityContext)
    }

    @GET
    @Path("/logout")
    fun logout(): Response = Response
        .temporaryRedirect(UriBuilder.fromPath(loginPage).build())
        .cookie(NewCookie(cookieName, null, "/", null, NewCookie.DEFAULT_VERSION, null, 0, false))
        .cookie(NewCookie(redirectCookieName, null, "/", null, NewCookie.DEFAULT_VERSION, null, 0, false))
        .build()
}