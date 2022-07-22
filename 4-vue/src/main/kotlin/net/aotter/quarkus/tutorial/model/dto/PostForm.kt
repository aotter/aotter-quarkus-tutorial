package net.aotter.quarkus.tutorial.model.dto

import javax.validation.constraints.NotEmpty

data class PostForm(
    @field:NotEmpty(message = "標題不得為空")
    var title: String,

    @field:NotEmpty(message = "分類必須選擇")
    var category: String,

    @field:NotEmpty(message = "內容不得為空")
    var content: String
)
