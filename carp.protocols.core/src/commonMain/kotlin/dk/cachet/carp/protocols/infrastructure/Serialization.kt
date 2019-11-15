package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import dk.cachet.carp.protocols.domain.triggers.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*


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
    }
    polymorphic( DeviceRegistration::class )
    {
        DefaultDeviceRegistration::class with DefaultDeviceRegistration.serializer()
        AltBeaconDeviceRegistration::class with AltBeaconDeviceRegistration.serializer()
    }
    polymorphic( TaskDescriptor::class )
    {
        ConcurrentTask::class with ConcurrentTask.serializer()
    }
    polymorphic( Measure::class )
    {
        DataTypeMeasure::class with DataTypeMeasure.serializer()
        PhoneSensorMeasure::class with PhoneSensorMeasure.serializer()
    }
    polymorphic( Trigger::class )
    {
        StartOfStudyTrigger::class with StartOfStudyTrigger.serializer()
    }

    polymorphic( ProtocolServiceRequest::class )
    {
        ProtocolServiceRequest.Add::class with ProtocolServiceRequest.Add.serializer()
        ProtocolServiceRequest.Update::class with ProtocolServiceRequest.Update.serializer()
        ProtocolServiceRequest.GetBy::class with ProtocolServiceRequest.GetBy.serializer()
        ProtocolServiceRequest.GetAllFor::class with ProtocolServiceRequest.GetAllFor.serializer()
        ProtocolServiceRequest.GetVersionHistoryFor::class with ProtocolServiceRequest.GetVersionHistoryFor.serializer()
    }
}

/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.protocols] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createProtocolsSerializer( module: SerialModule = EmptyModule ): Json
    = createDefaultJSON( PROTOCOLS_SERIAL_MODULE + module )

/**
 * A default CARP infrastructure serializer capable of serializing all [dk.cachet.carp.protocols] types.
 * In case custom extending types are defined, this variable should be reassigned for serialization extension functions to work as expected.
 * [createProtocolsSerializer] can be used to this end, by including all extending types in the [SerialModule] as parameter.
 */
var JSON: Json = createProtocolsSerializer()


/**
 * Create a [StudyProtocolSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.Companion.fromJson( json: String ): StudyProtocolSnapshot
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.toJson(): String
    = JSON.stringify( StudyProtocolSnapshot.serializer(), this )

/**
 * Create a [DeviceRegistration] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceRegistration.Companion.fromJson( json: String ): DeviceRegistration
    = JSON.parse( DeviceRegistrationSerializer, json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceRegistration.toJson(): String
    = JSON.stringify( DeviceRegistrationSerializer, this )