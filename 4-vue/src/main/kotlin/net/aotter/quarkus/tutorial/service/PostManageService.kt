package net.aotter.quarkus.tutorial.service

import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.PostSummary

interface PostManageService {
    suspend fun getSelfPostSummary(
        username: String,
        category: String?, authorIdValue: String?,
        page: Long, show: Int): PageData<PostSummary>
}