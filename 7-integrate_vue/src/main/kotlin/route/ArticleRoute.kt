package route

import io.smallrye.mutiny.Uni
import model.po.Article
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import repository.ArticleRepository
import repository.ArticleRepository.ArticleView
import security.Role
import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext

@Path("/")
class ArticleRoute: BaseRoute() {

    @Inject
    lateinit var articleRepository: ArticleRepository

    @POST
    @Path("article")
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun createArticle(
        @Context securityContext: SecurityContext,
        @RequestBody body: Article): Uni<Article?> {
        val author = securityContext.userPrincipal.name
        return uni {
            articleRepository.createArticle(body, author)
        }
    }

    @GET
    @Path("article")
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun getArticleById(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String): Uni<Article?> {
        val author = securityContext.userPrincipal.name
        return uni {
            articleRepository.getArticleById(articleId)
        }
    }

    @PUT
    @Path("article")
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun editArticle(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String,
        @RequestBody body: Article): Uni<Article?> {
        val author = securityContext.userPrincipal.name
        return uni {
            articleRepository.updateArticle(articleId,body)
        }
    }

    @DELETE
    @Path("article")
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun deleteArticle(
        @Context securityContext: SecurityContext,
        @QueryParam("articleId") articleId: String): Uni<Article?> {
        val author = securityContext.userPrincipal.name
        return uni {
            articleRepository.deleteArticle(articleId)
        }
    }

    @GET
    @Path("articles")
    @RolesAllowed(Role.ADMIN_CONSTANT, Role.USER_CONSTANT)
    fun getArticleListByUser(
        @Context securityContext: SecurityContext,
        @QueryParam("page") page: Int?): Uni<List<ArticleView>> {
        val author = securityContext.userPrincipal.name
        return uni {
            articleRepository.getArticleListByUser(author,page?:1)
        }
    }
}