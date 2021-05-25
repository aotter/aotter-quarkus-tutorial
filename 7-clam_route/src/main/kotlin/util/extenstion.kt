package util

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*

fun Vertx.launch(fn: suspend () -> Unit) {
    CoroutineScope(this.dispatcher()).launch { fn() }
}

fun <T> Vertx.async(fn: suspend () -> T): Deferred<T> {
    return CoroutineScope(this.dispatcher()).async { fn() }
}

fun <T> Vertx.uni(fn: suspend () -> T): Uni<T> = this.async(fn).asUni()
