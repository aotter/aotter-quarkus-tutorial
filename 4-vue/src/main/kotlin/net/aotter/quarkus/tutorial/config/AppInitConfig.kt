package net.aotter.quarkus.tutorial.config

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.runtime.Startup
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.repository.PostRepository
import net.aotter.quarkus.tutorial.repository.UserRepository
import net.aotter.quarkus.tutorial.security.Role
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@Startup
@ApplicationScoped
class AppInitConfig{
    @Inject
    lateinit var postRepository: PostRepository
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var logger: Logger

    @PostConstruct
     fun onStart() {
        initPostData()
        initUserData()
    }

    private fun initPostData(){
        val categoryList = arrayListOf("分類三","分類一","分類二")
        val posts = mutableListOf<Post>()
        for(index in 1..7){
            val post = Post(
                authorId = ObjectId("6278b21b245917288cd7220b"),
                authorName = "user",
                title = """Title $index""",
                category = categoryList[index % 3],
                content = """Content $index""",
                published = true,
                deleted = false
            )
            posts.add(post)
        }
        postRepository.count()
            .subscribe().with{
                if(it == 0L){
                    postRepository.persistOrUpdate(posts)
                        .onItemOrFailure()
                        .transform{ _, t ->
                            if(t != null){
                                logger.error("post insert failed")
                            }else{
                                logger.info("post insert successfully")
                            }
                        }.subscribe().with{ /** ignore **/ }
                }
            }
    }

    private fun initUserData(){
        val admin = User(
            username = "admin",
            credentials = BcryptUtil.bcryptHash("admin@123"),
            role = Role.ADMIN,
            deleted = false
        )
        val user = User(
            username = "user",
            credentials = BcryptUtil.bcryptHash("user@123"),
            role = Role.USER,
            deleted = false
        )
        userRepository.count()
            .subscribe().with {
                if(it == 0L){
                    userRepository.persistOrUpdate(admin, user)
                        .onItemOrFailure()
                        .transform{_, t ->
                            if(t!= null){
                                logger.error("user insert failed")
                            }else{
                                logger.info("user insert successfully")
                            }
                        }.subscribe().with { /** ignore **/ }
                }
            }
    }
}

