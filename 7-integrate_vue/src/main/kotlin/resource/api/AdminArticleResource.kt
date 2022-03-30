package resource.api

import com.mongodb.client.model.Filters
import io.quarkus.security.identity.SecurityIdentity
import model.dto.ArticleReq
import model.po.Article
import model.vo.ArticleListResponse
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import repository.ArticleRepository
import repository.UserRepository
import security.Role
import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.security.auth.login.LoginException
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext

@RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("api/admin")
class AdminArticleResource {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var articleRepository: ArticleRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var identity: SecurityIdentity

    @POST
    @Path("article")
    suspend fun create(
        @Context securityContext: SecurityContext,
        @Valid req: ArticleReq): Article {
        val userName = securityContext.userPrincipal.name
        val user = userRepository.findByUsername(userName)
        if(user != null){
            return articleRepository.save(Article(user, req))
        }else{
            throw LoginException("user not exists")
        }
    }

    @GET
    @Path("article")
    suspend fun getArticleById(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String): Article {
        val result = articleRepository.findOne(Filters.eq("_id",ObjectId(articleId)))
        if(result!= null){
            return result
        }else{
            throw Exception("article not exists")
        }
    }

    @PUT
    @Path("article")
    suspend fun update(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String,
        @Valid req: ArticleReq): Article {
        val result = articleRepository.update(ObjectId(articleId),req)
        if(result!= null){
            return result
        }else{
            throw Exception("article update fail")
        }
    }

    @PUT
    @Path("update-publish-status")
    suspend fun publish(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String,
        @QueryParam("published") published: Boolean): Article {
        val result = articleRepository.updatePublishStatus(ObjectId(articleId),published)
        if(result!= null){
            return result
        }else{
            throw Exception("article publish fail")
        }
    }

    @DELETE
    @Path("article")
    suspend fun delete(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String): Article {
        val result = articleRepository.delete(ObjectId(articleId))
        if(result!= null){
            return result
        }else{
            throw Exception("article delete fail")
        }
    }

    @GET
    @Path("articles")
    suspend fun getArticleListByUser(
        @Context securityContext: SecurityContext,
        @QueryParam("page") @DefaultValue("1") page: Int,
        @DefaultValue("10") show: Int): List<ArticleListResponse> {
        val userName = securityContext.userPrincipal.name
        val id = userRepository.findByUsername(userName)?.id
        val list = articleRepository.find(id,null,null,page,show)
        val filters = listOfNotNull(
            Filters.eq(Article::author.name, id),
            Filters.eq(Article::visible.name, true)
        )
        val totalPage = articleRepository.getPageLength(filters)

        return articleRepository.convertToArticleReqList(totalPage, list)
    }

}