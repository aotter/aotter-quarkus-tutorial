package net.aotter.quarkus.tutorial.util

fun String.abbreviate(maxWidth: Int, abbrevMarker: String = "..."): String = takeIf { it.length > maxWidth }
    ?.let { "${it.take(maxWidth)}$abbrevMarker" }
    ?: this