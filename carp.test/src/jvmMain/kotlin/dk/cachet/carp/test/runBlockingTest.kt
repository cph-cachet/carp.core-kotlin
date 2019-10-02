package dk.cachet.carp.test

import kotlinx.coroutines.runBlocking

actual fun runBlockingTest(block: suspend () -> Unit) = runBlocking { block() }