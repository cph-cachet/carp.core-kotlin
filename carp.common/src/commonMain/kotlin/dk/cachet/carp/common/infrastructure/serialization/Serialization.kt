package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.*
import dk.cachet.carp.common.application.data.input.*
import dk.cachet.carp.common.application.data.input.elements.*
import dk.cachet.carp.common.application.devices.*
import dk.cachet.carp.common.application.sampling.*
import dk.cachet.carp.common.application.tasks.*
import dk.cachet.carp.common.application.triggers.*
import dk.cachet.carp.common.application.users.*
import kotlinx.serialization.*
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
        subclass( AngularVelocity::class )
        subclass( CompletedTask::class )
        subclass( ECG::class )
        subclass( EDA::class )
        subclass( Geolocation::class )
        subclass( HeartRate::class )
        subclass( InterbeatInterval::class )
        subclass( NonGravitationalAcceleration::class )
        // HACK: explicit serializer needs to be registered for object declarations due to limitation of the JS legacy backend.
        // https://github.com/Kotlin/kotlinx.serialization/issues/1138#issuecomment-707989920
        // This can likely be removed once we upgrade to the new IR backend.
        subclass( NoData::class, NoData.serializer() )
        subclass( PPG::class )
        subclass( SignalStrength::class )
        subclass( SensorSkinContact::class )
        subclass( StepCount::class )
        subclass( TriggeredTask::class )

        // InputDataType classes.
        subclass(
            CustomInput::class,
            CustomInputSerializer( String::class, Int::class )
        )
        subclass( Sex::class, PolymorphicEnumSerializer( Sex.serializer() ) )

        subclass( CustomData::class )
        default { DataSerializer }
    }
    polymorphic( InputElement::class )
    {
        subclass( SelectOne::class )
        subclass( Text::class )
    }


    // `devices` namespace.
    fun PolymorphicModuleBuilder<AnyPrimaryDeviceConfiguration>.registerPrimaryDeviceConfigurationSubclasses()
    {
        subclass( CustomProtocolDevice::class )
        subclass( Smartphone::class )

        subclass( CustomPrimaryDeviceConfiguration::class )
    }
    polymorphic( DeviceConfiguration::class )
    {
        subclass( AltBeacon::class )
        subclass( BLEHeartRateDevice::class )
        registerPrimaryDeviceConfigurationSubclasses()

        subclass( CustomDeviceConfiguration::class )
        default { DeviceConfigurationSerializer }
    }
    polymorphic( PrimaryDeviceConfiguration::class )
    {
        registerPrimaryDeviceConfigurationSubclasses()

        default { PrimaryDeviceConfigurationSerializer }
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
        @Suppress( "UNCHECKED_CAST" )
        subclass(
            BatteryAwareSamplingConfiguration::class,
            BatteryAwareSamplingConfiguration.serializer( PolymorphicSerializer( SamplingConfiguration::class ) )
                as KSerializer<BatteryAwareSamplingConfiguration<*>>
        )
        subclass( GranularitySamplingConfiguration::class )
        subclass( IntervalSamplingConfiguration::class )
        subclass( NoOptionsSamplingConfiguration::class, NoOptionsSamplingConfiguration.serializer() )

        subclass( CustomSamplingConfiguration::class )
        default { SamplingConfigurationSerializer }
    }


    // `tasks` namespace.
    polymorphic( TaskConfiguration::class )
    {
        subclass( BackgroundTask::class )
        subclass( CustomProtocolTask::class )
        subclass( WebTask::class )

        subclass( CustomTaskConfiguration::class )
        default { TaskConfigurationSerializer }
    }


    // `triggers` namespace.
    polymorphic( TriggerConfiguration::class )
    {
        subclass( ElapsedTimeTrigger::class )
        subclass( ManualTrigger::class )
        subclass( ScheduledTrigger::class )

        subclass( CustomTriggerConfiguration::class )
        default { TriggerConfigurationSerializer }
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
const val CLASS_DISCRIMINATOR: String = "__type"

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
    }
}
