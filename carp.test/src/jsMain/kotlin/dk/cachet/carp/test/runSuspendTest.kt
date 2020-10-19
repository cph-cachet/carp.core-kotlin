package dk.cachet.carp.test

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise


actual fun runSuspendTest( block: suspend () -> Unit ): dynamic = GlobalScope.promise { block() }
