package endpoint

import io.smallrye.mutiny.Uni
import model.po.ClamData
import model.po.State
import org.bson.Document
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import repository.ClamDataRepository
import route.BaseRoute
import security.Role
import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext

@Path("/api")
class ClamEndpoint: BaseRoute() {

    @Inject
    lateinit var clamDataRepository: ClamDataRepository

    @Path("{collection}")
    @POST
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun createNewEntity(
            @Context securityContext: SecurityContext,
            @PathParam("collection") collection: String,
            @RequestBody body: ClamData
    ){
        val author = securityContext.userPrincipal.name
        uni {
            clamDataRepository.create(collection, body, author)
        }
    }

    @Path("{collection}/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun getPublishedEntityById(
            @PathParam("collection") collection: String,
            @PathParam("id") id: String
    ) = uni { clamDataRepository.getPublishedEntityById(collection, id)}

    @Path("{collection}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun getPublishedEntityList(
            @PathParam("collection") collection: String
    ): Uni<List<Document>> {
        return uni { clamDataRepository.getPublishedEntities(collection) }
    }

    @Path("{collection}/{id}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Role.ADMIN_CONSTANT)
    fun updateExistingEntity(
            @PathParam("collection") collection: String,
            @PathParam("id") id: String,
            @RequestBody body: Document
    ): Uni<Document?> = uni { clamDataRepository.updateEntity(collection, id, body) }

    @Path("{collection}/{id}/state/{state}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Role.ADMIN_CONSTANT)
    fun updateExistingEntityState(
            @PathParam("collection") collection: String,
            @PathParam("id") id: String,
            @PathParam("state") state: String
    ): Uni<Document?> = uni {
        clamDataRepository.updateEntityState(collection, id, State.valueOf(state))
    }

    @Path("{collection}/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Role.ADMIN_CONSTANT)
    fun archiveEntity(
            @Context securityContext: SecurityContext,
            @PathParam("collection") collection: String,
            @PathParam("id") id: String
    ): Uni<Document?> = uni { clamDataRepository.updateEntityState(collection, id, State.ARCHIVED) }

}