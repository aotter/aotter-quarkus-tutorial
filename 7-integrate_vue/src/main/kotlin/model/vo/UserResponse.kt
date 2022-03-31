package model.vo

import model.po.User
import security.Role

data class UserResponse (
    var username: String? = null,
    var role: Role? = Role.USER
){
    constructor(user: User): this(
        username = user.username,
        role = user.role
    )
}