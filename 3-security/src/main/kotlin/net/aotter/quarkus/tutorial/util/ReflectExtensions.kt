package net.aotter.quarkus.tutorial.util

import org.bson.codecs.pojo.annotations.BsonProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

fun <T, R> KProperty1<T, R>.bsonFieldName() = this.javaField
    ?.getAnnotation(BsonProperty::class.java)
    ?.let { it.value }
    ?: this.name

