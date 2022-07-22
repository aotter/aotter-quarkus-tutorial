package net.aotter.quarkus.tutorial.model.exception

open class BusinessException: RuntimeException{
    constructor(message: String): super(message)
}