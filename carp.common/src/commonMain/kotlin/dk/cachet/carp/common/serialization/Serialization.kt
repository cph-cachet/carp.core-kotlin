package dk.cachet.carp.common.serialization

import dk.cachet.carp.common.data.Acceleration
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.ECG
import dk.cachet.carp.common.data.FreeFormText
import dk.cachet.carp.common.data.Geolocation
import dk.cachet.carp.common.data.HeartRate
import dk.cachet.carp.common.data.RRInterval
import dk.cachet.carp.common.data.SensorSkinContact
import dk.cachet.carp.common.data.StepCount
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.common.users.UsernameAccountIdentity
import kotlinx.serialization.json.Json
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

    polymorphic( Data::class )
    {
        subclass( Acceleration::class )
        subclass( ECG::class )
        subclass( FreeFormText::class )
        subclass( Geolocation::class )
        subclass( HeartRate::class )
        // HACK: explicit serializer needs to be registered for object declarations due to limitation of the JS legacy backend.
        // https://github.com/Kotlin/kotlinx.serialization/issues/1138#issuecomment-707989920
        // This can likely be removed once we upgrade to the new IR backend.
        subclass( RRInterval::class, RRInterval.serializer() )
        subclass( SensorSkinContact::class )
        subclass( StepCount::class )
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
fun createDefaultJSON( module: SerializersModule? = null ): Json
{
    val jsonSerializersModule = if ( module == null ) COMMON_SERIAL_MODULE else COMMON_SERIAL_MODULE + module

    return Json {
        classDiscriminator = CLASS_DISCRIMINATOR
        serializersModule = jsonSerializersModule
        // TODO: `encodeDefaults` changed in kotlinx.serialization 1.0.0-RC2 to false by default
        //  which caused unknown polymorphic serializer tests to fail. Verify whether we need this.
        encodeDefaults = true
    }
}
