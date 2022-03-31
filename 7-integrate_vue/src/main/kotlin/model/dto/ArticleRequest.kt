package model.dto
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class ArticleRequest (

    @field:[
    NotNull(message = "category may not be null")
    NotBlank(message = "category may not be blank")
    ]
    var category: String? = null,

    @field:[
    NotNull(message = "title may not be null")
    NotBlank(message = "title may not be blank")
    ]
    var title: String? = null,

    @field:[
    NotNull(message = "content may not be null")
    ]
    var content: String? = null,
)