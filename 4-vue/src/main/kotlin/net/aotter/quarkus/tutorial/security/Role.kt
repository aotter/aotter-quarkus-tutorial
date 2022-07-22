package net.aotter.quarkus.tutorial.security

enum class Role {
    USER,
    ADMIN;
    companion object{
        val USER_VALUE = "USER"
        val ADMIN_VALUE = "ADMIN"
    }
}