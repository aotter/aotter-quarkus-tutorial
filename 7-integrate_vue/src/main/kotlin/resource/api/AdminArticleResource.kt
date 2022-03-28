package resource.api

import com.mongodb.client.model.Filters
import io.quarkus.security.identity.SecurityIdentity
import model.dto.ArticleReq
import model.po.Article
import model.po.User
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import repository.ArticleRepository
import repository.UserRepository
import route.BaseRoute
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
@Path("api/admin/article")
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
    suspend fun getArticleById(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String): Article {
        val result = articleRepository.findOne(Filters.eq("_id",articleId))
        if(result!= null){
            return result
        }else{
            throw Exception("article not exists")
        }
    }

    @PUT
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

    @DELETE
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

    @Path("articles")
    suspend fun getArticleListByUser(
        @Context securityContext: SecurityContext,
        @QueryParam("page") page: Int?): List<Article> {
        val userName = securityContext.userPrincipal.name
        val id = userRepository.findByUsername(userName)?.id
        return articleRepository.findPublished(id,null,page?:1)
    }

}