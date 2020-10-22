package dk.cachet.carp.test

import kotlinx.coroutines.runBlocking


actual fun runSuspendTest( block: suspend () -> Unit ) = runBlocking { block() }
