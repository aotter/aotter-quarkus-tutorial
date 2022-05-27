package net.aotter.quarkus.tutorial.service

import com.mongodb.MongoWriteException
import io.quarkus.elytron.security.common.BcryptUtil
import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.model.exception.BusinessException
import net.aotter.quarkus.tutorial.model.exception.DataException
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.repository.UserRepository
import net.aotter.quarkus.tutorial.security.Role
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class UserService {

    @Inject
    lateinit var userRepository: UserRepository

    suspend fun createUser(username: String, password: String, checkedPassword: String){
        if(password != checkedPassword){
            throw BusinessException("密碼與確認密碼不相符")
        }
        val user = User(
            username = username,
            credentials = BcryptUtil.bcryptHash(password),
            role = Role.USER,
            deleted = false,
        )
        try{
            userRepository.persist(user).awaitSuspending()
        }catch (e: MongoWriteException){
            if(e.error.code == 11000){
                throw BusinessException("使用者名稱已存在")
            }else{
                throw DataException(e.message ?: "")
            }
        }
    }
}