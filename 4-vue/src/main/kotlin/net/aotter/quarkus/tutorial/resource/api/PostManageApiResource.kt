package net.aotter.quarkus.tutorial.resource.api

import io.quarkus.security.Authenticated
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.PostForm
import net.aotter.quarkus.tutorial.model.dto.PublishPostRequest
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.security.Role
import net.aotter.quarkus.tutorial.service.PostManageService
import javax.inject.Inject
import javax.inject.Named
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext

@Authenticated
@Path("/api/post-manage")
class PostManageApiResource {

    @Inject
    @field:Named("UserPostManageService")
    lateinit var userPostManageService: PostManageService

    @Inject
    @field:Named("AdminPostManageService")
    lateinit var adminPostManageService: PostManageService

    @GET
    suspend fun listSelfPostSummary(
        @Context securityContext: SecurityContext,
        @QueryParam("authorName") authorName: String?,
        @QueryParam("category") category: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("10") show: Int
    ): ApiResponse<PageData<PostSummary>> {
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        val result = postManageService.getSelfPostSummary(username, category, authorName, page, show)
        return ApiResponse("成功", result)
    }

    @GET
    @Path("/{id}")
    suspend fun getSelfPostDetail(
        @Context securityContext: SecurityContext,
        @PathParam("id") id: String
    ): ApiResponse<PostDetail>{
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        val result = postManageService.getSelfPostDetail(username, id)
        return ApiResponse("成功", result)
    }

    @POST
    suspend fun createPost(
        @Context securityContext: SecurityContext,
        @Valid form: PostForm
    ): ApiResponse<Unit>{
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        postManageService.createPost(username, form.category, form.title, form.content)
        return ApiResponse("成功")
    }


    @PUT
    @Path("/{id}")
    suspend fun updateSelfPost(
        @Context securityContext: SecurityContext,
        @PathParam("id") id: String,
        @Valid form: PostForm
    ): ApiResponse<Unit>{
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        postManageService.updateSelfPost(username,  id , form.category, form.title, form.content)
        return ApiResponse("成功")
    }

    @PUT
    @Path("/{id}/published")
    suspend fun publishSelfPost(
        @Context securityContext: SecurityContext,
        @PathParam("id") id: String,
        @Valid request: PublishPostRequest
    ): ApiResponse<Unit>{
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        postManageService.publishSelfPost(username, id, request.status)
        return ApiResponse("成功")
    }

    @DELETE
    @Path("/{id}")
    suspend fun deleteSelfPost(
        @Context securityContext: SecurityContext,
        @PathParam("id") id: String
    ): ApiResponse<Unit>{
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        postManageService.deleteSelfPost(username, id)
        return ApiResponse("成功")
    }

    private fun getPostManageServiceByRole(securityContext: SecurityContext) =
        if (securityContext.isUserInRole(Role.ADMIN_VALUE)) {
            adminPostManageService
        } else {
            userPostManageService
        }
}