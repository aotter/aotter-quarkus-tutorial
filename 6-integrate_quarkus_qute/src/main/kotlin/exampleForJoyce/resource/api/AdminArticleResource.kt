package exampleForJoyce.resource.api

import exampleForJoyce.model.dto.ArticleReq
import exampleForJoyce.model.po.Article
import exampleForJoyce.repository.ArticleRepository
import model.po.User
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("api/admin/article")
//@RolesAllowed(Role.ADMIN)
class AdminArticleResource {

    @Inject
    lateinit var articleRepository: ArticleRepository


    @POST
    suspend fun create(@Valid req: ArticleReq): Article {
        // fixme: find current login user
        return articleRepository.save(Article(User(), req))
    }


    // TODO admin api calls



}


