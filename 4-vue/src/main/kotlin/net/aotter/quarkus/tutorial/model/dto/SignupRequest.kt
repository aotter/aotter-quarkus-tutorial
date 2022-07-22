package net.aotter.quarkus.tutorial.model.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class SignupRequest(
    @field:[
        NotEmpty(message = "使用者名稱不得為空")
    ]
    var username: String,

    @field:[
        NotEmpty(message = "密碼不得為空")
        Size(min = 8, max = 16, message = "密碼長度需為 8 到 16")
    ]
    var password: String,

    @field:[
        NotEmpty(message = "確認密碼不得為空")
        Size(min = 8, max = 16, message = "密碼長度需為 8 到 16")
    ]
    var checkedPassword: String
)
