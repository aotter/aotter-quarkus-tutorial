package security

enum class Role {
    ADMIN,
    USER;

    companion object {
        const val ADMIN_CONSTANT = "ADMIN"
        const val USER_CONSTANT = "USER"
    }
}