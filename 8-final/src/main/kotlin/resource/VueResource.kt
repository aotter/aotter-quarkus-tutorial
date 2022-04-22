package resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.quarkus.qute.runtime.TemplateProducer
import repository.UserRepository
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Consumes(MediaType.TEXT_HTML)
@Produces(MediaType.TEXT_HTML)
@Path("/")
class VueResource {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var templateProducer: TemplateProducer

    @CheckedTemplate(requireTypeSafeExpressions = false)
    object Templates {
        @JvmStatic
        external fun index(): TemplateInstance
    }

    @GET
    @Path("/console/")
    fun index(): TemplateInstance = Templates.index()
}