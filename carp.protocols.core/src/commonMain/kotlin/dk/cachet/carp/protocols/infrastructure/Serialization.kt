package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.ProtocolVersion
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfiguration
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import dk.cachet.carp.protocols.domain.devices.AltBeacon
import dk.cachet.carp.protocols.domain.devices.AltBeaconDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.CustomProtocolDevice
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationSerializer
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.protocols.domain.tasks.CustomProtocolTask
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.tasks.measures.DataTypeMeasure
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.domain.tasks.measures.PhoneSensorMeasure
import dk.cachet.carp.protocols.domain.triggers.ElapsedTimeTrigger
import dk.cachet.carp.protocols.domain.triggers.ManualTrigger
import dk.cachet.carp.protocols.domain.triggers.ScheduledTrigger
import dk.cachet.carp.protocols.domain.triggers.Trigger
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerialModule


/**
 * Types in the [dk.cachet.carp.protocols] module which need to be registered when using [Json] serializer.
 */
val PROTOCOLS_SERIAL_MODULE = SerializersModule {
    polymorphic( DeviceDescriptor::class )
    {
        AltBeacon::class with AltBeacon.serializer()
    }
    polymorphic( MasterDeviceDescriptor::class, DeviceDescriptor::class )
    {
        Smartphone::class with Smartphone.serializer()
        CustomProtocolDevice::class with CustomProtocolDevice.serializer()
    }
    polymorphic( SamplingConfiguration::class )
    {
        IntervalSamplingConfiguration::class with IntervalSamplingConfiguration.serializer()
    }
    polymorphic( DeviceRegistration::class )
    {
        DefaultDeviceRegistration::class with DefaultDeviceRegistration.serializer()
        AltBeaconDeviceRegistration::class with AltBeaconDeviceRegistration.serializer()
    }
    polymorphic( TaskDescriptor::class )
    {
        ConcurrentTask::class with ConcurrentTask.serializer()
        CustomProtocolTask::class with CustomProtocolTask.serializer()
    }
    polymorphic( Measure::class )
    {
        DataTypeMeasure::class with DataTypeMeasure.serializer()
        PhoneSensorMeasure::class with PhoneSensorMeasure.serializer()
    }
    polymorphic( Trigger::class )
    {
        ElapsedTimeTrigger::class with ElapsedTimeTrigger.serializer()
        ScheduledTrigger::class with ScheduledTrigger.serializer()
        ManualTrigger::class with ManualTrigger.serializer()
    }
}

/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.protocols] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createProtocolsSerializer( module: SerialModule = EmptyModule ): Json =
    createDefaultJSON( PROTOCOLS_SERIAL_MODULE + module )

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
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolOwner.toJson(): String =
    JSON.stringify( ProtocolOwner.serializer(), this )

/**
 * Create a [ProtocolVersion] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolVersion.Companion.fromJson( json: String ): ProtocolVersion =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolVersion.toJson(): String =
    JSON.stringify( ProtocolVersion.serializer(), this )

/**
 * Create a [StudyProtocolSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.Companion.fromJson( json: String ): StudyProtocolSnapshot =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.toJson(): String =
    JSON.stringify( StudyProtocolSnapshot.serializer(), this )

/**
 * Create a [DeviceRegistration] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceRegistration.Companion.fromJson( json: String ): DeviceRegistration =
    JSON.parse( DeviceRegistrationSerializer, json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceRegistration.toJson(): String =
    JSON.stringify( DeviceRegistrationSerializer, this )
