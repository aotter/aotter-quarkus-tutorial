package net.aotter.quarkus.tutorial.util.template

import io.quarkus.qute.TemplateExtension
import io.vertx.core.http.HttpServerRequest
import javax.ws.rs.core.UriBuilder

@TemplateExtension
class RouteExtensions {
    companion object{
        @JvmStatic
        fun toRouteBuilder(request: HttpServerRequest): UriBuilder = UriBuilder.fromUri(request.absoluteURI())
        @JvmStatic
        fun setRouteQueryParam(builder: UriBuilder, name: String, value: Any): UriBuilder  {
            return  builder.replaceQueryParam(name, value)
        }
        @JvmStatic
        fun getRouteQueryParam(request: HttpServerRequest, parameterName: String): String? = request.getParam(parameterName)
    }
}