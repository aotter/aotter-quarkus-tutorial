package net.aotter.quarkus.tutorial.repository

import net.aotter.quarkus.tutorial.model.po.Post
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostRepository: AuditingRepository<Post>(){
}
