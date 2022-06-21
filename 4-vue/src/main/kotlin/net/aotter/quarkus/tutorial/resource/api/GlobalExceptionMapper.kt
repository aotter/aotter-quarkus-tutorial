package net.aotter.quarkus.tutorial.resource.api

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import net.aotter.quarkus.tutorial.model.exception.BusinessException
import net.aotter.quarkus.tutorial.model.exception.DataException
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import javax.inject.Inject
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class GlobalExceptionMapper {
    @Inject
    lateinit var logger: Logger

    @ServerExceptionMapper
    fun mismatchedInputException(e: MismatchedInputException): Response = Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(ApiResponse<Unit>(message = "缺少請求參數"))
        .build()

    @ServerExceptionMapper
    fun missingKotlinParameterException(e: MissingKotlinParameterException): Response = Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(ApiResponse<Unit>(message = "缺少請求參數"))
        .build()

    @ServerExceptionMapper
    fun businessException(e: BusinessException): Response = Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(ApiResponse<Unit>(message = e.message ?: ""))
        .build()

    @ServerExceptionMapper
    fun dataException(e: DataException): Response {
        logger.error(e.message)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApiResponse<Unit>(message = "資料存取錯誤，請稍後再試或洽管理員"))
            .build()
    }

    @ServerExceptionMapper
    fun exception(e: Exception): Response{
        logger.error(e.message)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApiResponse<Unit>(message = "系統異常，請稍後再試或洽管理員"))
            .build()
    }
}