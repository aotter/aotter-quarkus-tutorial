package model.vo

import kotlin.math.ceil

data class BaseListResponse<T>(
    var list: List<T>,
    var page: Int,
    var show: Int,
    var total: Long,
    var totalPages: Int
){
    constructor(list: List<T>, page: Int, show: Int, total: Long): this(
        list = list,
        page = page,
        show = show,
        total = total,
        totalPages = ceil(total.toDouble() / 10).toInt()
    )
}