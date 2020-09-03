package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.RecurrenceRule
import dk.cachet.carp.common.TimeOfDay
import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.data.IntervalSamplingConfiguration
import dk.cachet.carp.protocols.domain.devices.AltBeacon
import dk.cachet.carp.protocols.domain.devices.AltBeaconDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.CustomProtocolDevice
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.protocols.domain.tasks.CustomProtocolTask
import dk.cachet.carp.protocols.domain.tasks.measures.DataTypeMeasure
import dk.cachet.carp.protocols.domain.tasks.measures.PhoneSensorMeasure
import dk.cachet.carp.protocols.domain.triggers.ElapsedTimeTrigger
import dk.cachet.carp.protocols.domain.triggers.ManualTrigger
import dk.cachet.carp.protocols.domain.triggers.ScheduledTrigger
import dk.cachet.carp.test.serialization.ConcreteTypesSerializationTest


private val protocolInstances = listOf(
    // Devices.
    Smartphone( "User's phone" ),
    AltBeacon( "Kitchen" ),
    CustomProtocolDevice( "User's phone" ),

    // Sampling configurations.
    IntervalSamplingConfiguration( TimeSpan.fromMilliseconds( 1000.0 ) ),

    // Device registrations.
    AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0 ),
    DefaultDeviceRegistration( "Some device" ),

    // Tasks.
    ConcurrentTask( "Start measures", listOf() ),
    CustomProtocolTask( "Custom study runtime", "<protocol definition>" ),

    // Measures.
    DataTypeMeasure( "dk.cachet.carp", "SomeType" ),
    PhoneSensorMeasure.geolocation(),

    // Triggers.
    ElapsedTimeTrigger( Smartphone( "User's phone" ), TimeSpan( 0 ) ),
    ScheduledTrigger(
        Smartphone( "User's phone"),
        TimeOfDay( 12 ), RecurrenceRule( RecurrenceRule.Frequency.DAILY )
    ),
    ManualTrigger(
        "User's phone",
        "Mood",
        "Describe how you are feeling at the moment."
    )
)

/**
 * Serialization tests for all extending types from base classes in [dk.cachet.carp.protocols].
 */
class SerializationTest : ConcreteTypesSerializationTest(
    createProtocolsSerializer(),
    PROTOCOLS_SERIAL_MODULE,
    protocolInstances )
