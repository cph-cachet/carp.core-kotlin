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
 * Create a [Json] serializer adopting a default CARP infrastructure configuration.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createDefaultJSON( module: SerializersModule = EmptySerializersModule ): Json
{
    return Json {
        // The default has this set to "false" and classDiscriminator set to "type".
        // But, this causes problems for types which include properties with the name "type" (i.e., Measure).
        // In addition, the custom [UnknownPolymorphicSerializer] also uses array polymorphism at the moment.
        // Therefore, continuing to use array polymorphism (as it was in older 'kotlinx.serialization' versions) is preferred.
        useArrayPolymorphism = true
        // TODO: This used to be enabled by default before, but is disabled by default in kotlinx.serialization 1.0.0.
        //  We should look at the serialized output of `DeviceDescriptor.samplingConfiguration` to determine whether we want this.
        allowStructuredMapKeys = true
        serializersModule = COMMON_SERIAL_MODULE + module
    }
}
