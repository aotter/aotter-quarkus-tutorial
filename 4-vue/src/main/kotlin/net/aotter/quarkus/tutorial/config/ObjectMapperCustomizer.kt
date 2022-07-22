package net.aotter.quarkus.tutorial.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.jackson.ObjectMapperCustomizer
import javax.inject.Singleton

@Singleton
class MyObjectMapperCustomizer: ObjectMapperCustomizer {
    override fun customize(objectMapper: ObjectMapper) {
        objectMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    }
}