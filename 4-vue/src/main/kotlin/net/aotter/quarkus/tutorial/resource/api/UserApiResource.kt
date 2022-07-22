package net.aotter.quarkus.tutorial.resource.api

import net.aotter.quarkus.tutorial.model.dto.SignupRequest
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import net.aotter.quarkus.tutorial.service.UserService
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/api")
class UserApiResource {
    @Inject
    lateinit var userService: UserService

    @Path("/user")
    @POST
    suspend fun signup(@Valid request: SignupRequest): ApiResponse<Void>{
        userService.createUser(request.username, request.password, request.checkedPassword)
        return ApiResponse(message = "會員註冊成功")
    }
}