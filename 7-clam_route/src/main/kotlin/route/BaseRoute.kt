package route

import io.smallrye.mutiny.Uni
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import util.uni
import java.net.URI
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response

import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider


@ApplicationScoped
abstract class BaseRoute {

    @Inject
    lateinit var vertx: Vertx

    fun <T> uni(fn: suspend () -> T): Uni<T> = vertx.uni(fn)

    fun init(@Observes router: Router) {
        router.errorHandler(404) { routingContext: RoutingContext -> routingContext.response().setStatusCode(302).putHeader("Location", "/index.html").end() }
    }

}
