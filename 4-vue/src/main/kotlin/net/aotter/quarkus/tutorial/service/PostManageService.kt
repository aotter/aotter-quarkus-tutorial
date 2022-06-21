package net.aotter.quarkus.tutorial.service

import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.PostSummary

interface PostManageService {
    suspend fun getSelfPostSummary(
        username: String,
        category: String?, authorName: String?,
        page: Long, show: Int): PageData<PostSummary>

    suspend fun publishSelfPost(username: String, idValue: String, status: Boolean)

    suspend fun deleteSelfPost(username: String, idValue: String)
}