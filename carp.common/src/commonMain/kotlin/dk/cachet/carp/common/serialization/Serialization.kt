package dk.cachet.carp.common.serialization

import kotlinx.serialization.json.*


/**
 * The main application-wide entry point for Json serialization.
 * This ensures a global configuration on how serialization should occur.
 */
val JSON: Json = Json( JsonConfiguration.Stable )