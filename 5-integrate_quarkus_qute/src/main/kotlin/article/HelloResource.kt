package article

import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType


@Path("/hello")
class HelloResource {
    @Inject
    lateinit var hello: Template

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    operator fun get(@QueryParam("name") name: String?): TemplateInstance {
        return hello.data("name", name)
    }
}






//    @CheckedTemplate
//    object Templates {
//        @JvmStatic
//        external fun hello(name: String?): TemplateInstance?
//    }
//
//    @GET
//    @Consumes(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.TEXT_PLAIN)
//    operator fun get(@QueryParam("name") name: String?): TemplateInstance? {
//        return Templates.hello(name)
//    }