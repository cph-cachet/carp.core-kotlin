@file:Suppress( "TooManyFunctions", "WildcardImport" )

package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.sampling.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import dk.cachet.carp.protocols.domain.triggers.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.subclass


/**
 * Types in the [dk.cachet.carp.protocols] module which need to be registered when using [Json] serializer.
 */
val PROTOCOLS_SERIAL_MODULE = SerializersModule {
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
    polymorphic( SamplingConfiguration::class )
    {
        subclass( IntervalSamplingConfiguration::class )
        subclass( NoOptionsSamplingConfiguration::class )

        subclass( CustomSamplingConfiguration::class )
        default { SamplingConfigurationSerializer }
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
    polymorphic( TaskDescriptor::class )
    {
        subclass( ConcurrentTask::class )
        subclass( CustomProtocolTask::class )

        subclass( CustomTaskDescriptor::class )
        default { TaskDescriptorSerializer }
    }
    polymorphic( Measure::class )
    {
        subclass( DataTypeMeasure::class )
        subclass( PhoneSensorMeasure::class )

        subclass( CustomMeasure::class )
        default { MeasureSerializer }
    }
    polymorphic( Trigger::class )
    {
        subclass( ElapsedTimeTrigger::class )
        subclass( ManualTrigger::class )
        subclass( ScheduledTrigger::class )

        subclass( CustomTrigger::class )
        default { TriggerSerializer }
    }
}

/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.protocols] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createProtocolsSerializer( module: SerializersModule? = null ): Json
{
    val serializersModule =
        if ( module == null ) PROTOCOLS_SERIAL_MODULE
        else PROTOCOLS_SERIAL_MODULE + module

    return createDefaultJSON( serializersModule )
}

/**
 * A default CARP infrastructure serializer capable of serializing all [dk.cachet.carp.protocols] types.
 * In case custom extending types are defined, this variable should be reassigned for serialization extension functions to work as expected.
 * [createProtocolsSerializer] can be used to this end, by including all extending types in the [SerialModule] as parameter.
 */
var JSON: Json = createProtocolsSerializer()


/**
 * Create a [ProtocolOwner] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolOwner.Companion.fromJson( json: String ): ProtocolOwner =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolOwner.toJson(): String =
    JSON.encodeToString( ProtocolOwner.serializer(), this )

/**
 * Create a [ProtocolVersion] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolVersion.Companion.fromJson( json: String ): ProtocolVersion =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolVersion.toJson(): String =
    JSON.encodeToString( ProtocolVersion.serializer(), this )

/**
 * Create a [StudyProtocolSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.Companion.fromJson( json: String ): StudyProtocolSnapshot =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.toJson(): String =
    JSON.encodeToString( StudyProtocolSnapshot.serializer(), this )

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
