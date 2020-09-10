package dk.cachet.carp.common.serialization

import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.common.users.UsernameAccountIdentity
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.subclass


/**
 * Types in the [dk.cachet.carp.common] module which need to be registered when using [Json] serializer.
 */
val COMMON_SERIAL_MODULE = SerializersModule {
    polymorphic( AccountIdentity::class )
    {
        subclass( UsernameAccountIdentity::class )
        subclass( EmailAccountIdentity::class )
    }
}

/**
 * Name of the class descriptor property for polymorphic serialization.
 */
const val CLASS_DISCRIMINATOR: String = "\$type"

/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createDefaultJSON( module: SerializersModule = EmptySerializersModule ): Json
{
    return Json {
        classDiscriminator = CLASS_DISCRIMINATOR
        serializersModule = COMMON_SERIAL_MODULE + module
    }
}
