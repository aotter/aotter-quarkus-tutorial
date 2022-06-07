package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.quarkus.security.Authenticated
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.TEXT_HTML)
class ConsoleResource {

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun index(): TemplateInstance
    }

    @Authenticated
    @Path("/console/")
    @GET
    fun index(): TemplateInstance = Templates.index()
}