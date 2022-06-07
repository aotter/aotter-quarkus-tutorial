package net.aotter.quarkus.tutorial.resource.api

import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.security.Role
import net.aotter.quarkus.tutorial.service.PostManageService
import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext

@Path("/api")
class PostManageApiResource {

    @Inject
    @field:Named("UserPostManageService")
    lateinit var userPostManageService: PostManageService

    @Inject
    @field:Named("AdminPostManageService")
    lateinit var adminPostManageService: PostManageService

    @GET
    @Path("/posts")
    suspend fun listSelfPost(
        @Context securityContext: SecurityContext,
        @QueryParam("authorId") authorId: String?,
        @QueryParam("category") category: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("10") show: Int
    ): ApiResponse<PageData<PostSummary>> {
        val postManageService = if(securityContext.isUserInRole(Role.ADMIN_VALUE)){
            adminPostManageService
        }else{
            userPostManageService
        }
        val username = securityContext.userPrincipal.name
        val result = postManageService.getSelfPostSummary(username, authorId, category, page, show)
        return ApiResponse("成功", result)
    }

}