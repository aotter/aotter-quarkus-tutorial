package resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.quarkus.qute.runtime.TemplateProducer
import io.smallrye.mutiny.Uni
import model.po.User
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import repository.ArticleRepository
import repository.UserRepository
import security.Role
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Consumes(MediaType.TEXT_HTML)
@Produces(MediaType.TEXT_HTML)
@Path("/rest")
class UserResource {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var templateProducer: TemplateProducer

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun login(): TemplateInstance

        @JvmStatic
        external fun signup(): TemplateInstance
    }

    @GET
    @Path("login")
    fun login(): TemplateInstance = Templates.login()

    @GET
    @Path("signup")
    fun signup(): TemplateInstance = Templates.signup()

}