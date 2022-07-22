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
fun <T, R> PageData<T>.map(transform: (T) -> R): PageData<R> = PageData(list.map(transform), page, show, total)