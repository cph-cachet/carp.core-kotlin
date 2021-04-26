@file:Suppress( "WildcardImport" )

package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.*
import dk.cachet.carp.common.application.data.input.*
import dk.cachet.carp.common.application.data.input.elements.*
import dk.cachet.carp.common.application.devices.*
import dk.cachet.carp.common.application.sampling.*
import dk.cachet.carp.common.application.tasks.*
import dk.cachet.carp.common.application.triggers.*
import dk.cachet.carp.common.application.users.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*


/**
 * Types in the [dk.cachet.carp.common] module which need to be registered when using [Json] serializer.
 */
val COMMON_SERIAL_MODULE = SerializersModule {
    // `data` namespace.
    polymorphic( Data::class )
    {
        // DataType classes.
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

        // InputDataType classes.
        subclass(
            CustomInput::class,
            CustomInputSerializer( String::class, Int::class )
        )
        subclass( Sex::class, PolymorphicEnumSerializer( Sex.serializer() ) )
    }
    polymorphic( InputElement::class )
    {
        subclass( SelectOne::class )
        subclass( Text::class )
    }


    // `devices` namespace.
    fun PolymorphicModuleBuilder<AnyMasterDeviceDescriptor>.registerMasterDeviceDescriptorSubclasses()
    {
        subclass( CustomProtocolDevice::class )
        subclass( Smartphone::class )

        subclass( CustomMasterDeviceDescriptor::class )
    }
    polymorphic( DeviceDescriptor::class )
    {
        subclass( AltBeacon::class )
        subclass( BLEHeartRateSensor::class )
        registerMasterDeviceDescriptorSubclasses()

        subclass( CustomDeviceDescriptor::class )
        default { DeviceDescriptorSerializer }
    }
    polymorphic( MasterDeviceDescriptor::class )
    {
        registerMasterDeviceDescriptorSubclasses()

        default { MasterDeviceDescriptorSerializer }
    }
    polymorphic( DeviceRegistration::class )
    {
        subclass( AltBeaconDeviceRegistration::class )
        subclass( BLESerialNumberDeviceRegistration::class )
        subclass( DefaultDeviceRegistration::class )
        subclass( MACAddressDeviceRegistration::class )

        subclass( CustomDeviceRegistration::class )
        default { DeviceRegistrationSerializer }
    }


    // `sampling` namespace.
    polymorphic( SamplingConfiguration::class )
    {
        subclass( IntervalSamplingConfiguration::class )
        subclass( NoOptionsSamplingConfiguration::class, NoOptionsSamplingConfiguration.serializer() )

        subclass( CustomSamplingConfiguration::class )
        default { SamplingConfigurationSerializer }
    }


    // `tasks` namespace.
    polymorphic( TaskDescriptor::class )
    {
        subclass( BackgroundTask::class )
        subclass( CustomProtocolTask::class )

        subclass( CustomTaskDescriptor::class )
        default { TaskDescriptorSerializer }
    }


    // `triggers` namespace.
    polymorphic( Trigger::class )
    {
        subclass( ElapsedTimeTrigger::class )
        subclass( ManualTrigger::class )
        subclass( ScheduledTrigger::class )

        subclass( CustomTrigger::class )
        default { TriggerSerializer }
    }


    // `users` namespace.
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
 * A default CARP infrastructure serializer capable of serializing all [dk.cachet.carp.common] types.
 * In case custom extending types are defined, this variable should be reassigned for serialization extension functions to work as expected.
 * [createDefaultJSON] can be used to this end, by including all extending types in the [SerializersModule] as parameter.
 */
var JSON: Json = createDefaultJSON()


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

/**
 * Create a [DeviceRegistration] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceRegistration.Companion.fromJson( json: String ): DeviceRegistration =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceRegistration.toJson(): String =
    JSON.encodeToString( DeviceRegistration.serializer(), this )
