package net.aotter.quarkus.tutorial.model.dto

import kotlin.math.ceil

data class PageData<T>(
    var list: List<T>,
    var page: Long,
    var show: Int,
    var total: Long,
    var totalPages: Long
){
    constructor(list: List<T>, page: Long, show: Int, total: Long): this(
        list, page, show, total, ceil(total.toDouble() / show).toLong()
    )
}