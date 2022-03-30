package security

enum class Role(val role: String) {
    ADMIN("ADMIN"),
    USER("USER");

    companion object {
        const val ADMIN_CONSTANT = "ADMIN"
        const val USER_CONSTANT = "USER"
    }
}

sealed class Roles(val role: String){
    object ADMIN: Roles("ADMIN")
    object USER: Roles("USER")
}