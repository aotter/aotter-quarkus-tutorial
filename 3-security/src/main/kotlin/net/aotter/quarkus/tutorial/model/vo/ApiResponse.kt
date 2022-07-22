package net.aotter.quarkus.tutorial.model.vo

import com.fasterxml.jackson.annotation.JsonInclude

data class ApiResponse<T>(
    var message: String,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    var data: T? = null
)
