package net.aotter.quarkus.tutorial.repository

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.util.bsonFieldName
import org.jboss.logging.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class UserRepository: AuditingRepository<User>() {
    @Inject
    lateinit var logger: Logger

    @PostConstruct
    fun init(){
        mongoCollection().createIndex(
            Indexes.ascending(User::username.bsonFieldName()),
            IndexOptions().unique(true)
        )
            .onItemOrFailure()
            .transform { result, t ->
                if (t != null) {
                    logger.error("collection ${mongoCollection().documentClass.simpleName} index creation failed: ${t.message}")
                } else {
                    logger.info("collection ${mongoCollection().documentClass.simpleName} index creation: $result")
                }
            }.subscribe().with { /** ignore **/ }
    }

    fun findOneByDeletedIsFalseAndUsername(username: String): Uni<User?>{
        val criteria = HashMap<String, Any>().apply {
            put(User::deleted.name, false)
            put(User::username.name, username)
        }
        return findByCriteria(criteria).firstResult()
    }
}