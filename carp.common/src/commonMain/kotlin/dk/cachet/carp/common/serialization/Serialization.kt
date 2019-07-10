package dk.cachet.carp.common.serialization

import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*


/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 *
 * TODO: Some parts of the domain model still depend on this. Serialization should be fully decoupled from the domain model.
 */
fun createDefaultJSON( module: SerialModule = EmptyModule ): Json
{
    return Json( JsonConfiguration.Stable, module )
}