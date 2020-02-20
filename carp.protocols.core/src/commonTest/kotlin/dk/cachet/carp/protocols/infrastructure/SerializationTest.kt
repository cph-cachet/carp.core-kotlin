package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.devices.AltBeacon
import dk.cachet.carp.protocols.domain.devices.AltBeaconDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.protocols.domain.tasks.measures.DataTypeMeasure
import dk.cachet.carp.protocols.domain.tasks.measures.PhoneSensorMeasure
import dk.cachet.carp.protocols.domain.triggers.StartOfStudyTrigger
import dk.cachet.carp.test.serialization.ConcreteTypesSerializationTest


private val protocolInstances = listOf(
    // Devices.
    Smartphone( "User's phone" ),
    AltBeacon( "Kitchen" ),

    // Device registrations.
    AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0 ),
    DefaultDeviceRegistration( "Some device" ),

    // Tasks.
    ConcurrentTask( "Start measures", listOf() ),

    // Measures.
    DataTypeMeasure( "dk.cachet.carp", "SomeType" ),
    PhoneSensorMeasure.geolocation(),

    // Triggers.
    StartOfStudyTrigger( Smartphone( "User's phone" ) )
)

/**
 * Serialization tests for all extending types from base classes in [dk.cachet.carp.protocols].
 */
class SerializationTest : ConcreteTypesSerializationTest(
    createProtocolsSerializer(),
    PROTOCOLS_SERIAL_MODULE,
    protocolInstances )
