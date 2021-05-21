package endpoint

import com.fasterxml.jackson.annotation.JsonProperty
import io.smallrye.mutiny.Uni
import model.po.User
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import route.BaseRoute
import security.Role
import javax.annotation.security.RolesAllowed
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.Context
import javax.ws.rs.core.NewCookie
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

@Path("rest")
class UserEndpoint: BaseRoute() {

    data class SignUpRequest(
            @JsonProperty("username") var username: String,
            @JsonProperty("password") var password: String
    )
    @Path("signUp")
    @POST
    fun signUp(@RequestBody body: SignUpRequest): Uni<User>{
        val (username, password) = body
        return uni {
            User.create(username.trim().toLowerCase(), password, mutableSetOf(Role.USER))
        }
    }

    @Path("me")
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    @GET
    fun user(@Context securityContext: SecurityContext): Uni<String> = uni {
        "user ${securityContext.userPrincipal.name}"
    }

    @Path("admin")
    @RolesAllowed(Role.ADMIN_CONSTANT)
    @GET
    fun admin(@Context securityContext: SecurityContext): Uni<String> = uni {
        "admin ${securityContext.userPrincipal.name}"
    }

    @Path("logout")
    @GET
    fun logout(): Response {
        return Response.ok()
                .cookie(NewCookie("quarkus-credential", null, "/", null, NewCookie.DEFAULT_VERSION, null, 0, false))
                .build()
    }

}