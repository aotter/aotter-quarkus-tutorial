package net.aotter.quarkus.tutorial.model.exception

open class DataException: RuntimeException {
    constructor(message: String): super(message)
}