package net.aotter.quarkus.tutorial.resource

import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import net.aotter.quarkus.tutorial.util.abbreviate
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.core.UriInfo

abstract class AbstractTemplateResource {
    @Context
    lateinit var uriInfo: UriInfo
    @Context
    lateinit var securityContext: SecurityContext

    fun buildHTMLMetaData(title: String, type: String, description: String, image: String = ""): HTMLMetaData {
        val url = uriInfo.baseUriBuilder.replaceQuery("").toTemplate()
        return HTMLMetaData(title, type, description.abbreviate(20), url, image)
    }
}