package resource.api

import com.fasterxml.jackson.annotation.JsonProperty
import model.po.User
import model.vo.UserResponse
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.jboss.logging.Logger
import repository.UserRepository
import security.Role
import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.security.auth.login.LoginException
import javax.ws.rs.*
import javax.ws.rs.core.*

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("api/rest/user")
class UserResource {
    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var userRepository: UserRepository

    data class SignUpRequest(
        @JsonProperty("username") var username: String,
        @JsonProperty("password") var password: String
    )

    @Path("signUp")
    @POST
    suspend fun signUp(@RequestBody body: SignUpRequest): User {
        val (username, password) = body
        return userRepository.create(username,password,Role.USER)
    }

    @Path("me")
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    @GET
    suspend fun user(@Context securityContext: SecurityContext): UserResponse{
        val userName = securityContext.userPrincipal.name
        val user = userRepository.findByUsername(userName)
        if(user != null){
            return UserResponse(user)
        }else{
            throw LoginException("user not exists")
        }
    }

    @Path("admin")
    @RolesAllowed(Role.ADMIN_CONSTANT)
    @GET
    fun admin(@Context securityContext: SecurityContext): String = "admin ${securityContext.userPrincipal.name}"

    @Path("logout")
    @GET
    fun logout(): Response {
        return Response.ok()
            .cookie(NewCookie("quarkus-credential", null, "/", null, NewCookie.DEFAULT_VERSION, null, 0, false))
            .build()
    }
}