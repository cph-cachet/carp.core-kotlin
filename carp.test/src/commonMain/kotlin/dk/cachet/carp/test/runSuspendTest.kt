package dk.cachet.carp.test


/**
 * Supports writing multiplatform tests with suspending functions.
 *
 * On JVM this runs as `runBlocking` and on JS as an asynchronous test:
 * https://youtrack.jetbrains.com/issue/KT-22228
 */
expect fun runSuspendTest( block: suspend () -> Unit )
