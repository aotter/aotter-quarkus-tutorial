package route

import io.smallrye.mutiny.Uni
import io.vertx.core.Vertx
import util.uni
import javax.inject.Inject


abstract class BaseRoute {

    @Inject
    lateinit var vertx: Vertx

    fun <T> uni(fn: suspend () -> T): Uni<T> = vertx.uni(fn)

}
