package dk.cachet.carp.common.serialization

import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*


/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createDefaultJSON( module: SerialModule = EmptyModule ): Json
{
    val configuration = JsonConfiguration.Stable.copy(
        // JsonConfiguration.STABLE has this set to "false" and classDiscriminator set to "type".
        // But, this causes problems for types which include properties with the name "type" (i.e., Measure).
        // In addition, the custom [UnknownPolymorphicSerializer] also uses array polymorphism at the moment.
        // Therefore, continuing to use array polymorphism (as it was in older 'kotlinx.serialization' versions) is preferred.
        useArrayPolymorphism = true )

    return Json( configuration, module )
}