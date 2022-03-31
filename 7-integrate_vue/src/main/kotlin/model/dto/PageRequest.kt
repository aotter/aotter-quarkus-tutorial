package model.dto

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam

open class PageRequest {
    @field:[
    QueryParam("page")
    DefaultValue("1")
    Min(1)
    Max(100)]
    var page: Int = 1

    @field:[
    QueryParam("show")
    DefaultValue("10")
    Min(1)
    Max(100)]
    var show: Int = 10
}