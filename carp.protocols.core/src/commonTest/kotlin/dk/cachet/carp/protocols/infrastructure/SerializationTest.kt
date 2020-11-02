@file:Suppress( "WildcardImport" )

package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.MACAddress
import dk.cachet.carp.common.RecurrenceRule
import dk.cachet.carp.common.TimeOfDay
import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.sampling.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import dk.cachet.carp.protocols.domain.triggers.*
import dk.cachet.carp.test.serialization.ConcreteTypesSerializationTest


private val protocolInstances = listOf(
    // Devices.
    AltBeacon( "Kitchen" ),
    BLEHeartRateSensor( "Polar" ),
    CustomProtocolDevice( "User's phone" ),
    Smartphone( "User's phone" ),

    // Sampling configurations.
    IntervalSamplingConfiguration( TimeSpan.fromMilliseconds( 1000.0 ) ),
    NoOptionsSamplingConfiguration(),

    // Device registrations.
    AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0 ),
    BLESerialNumberDeviceRegistration( "123456789" ),
    DefaultDeviceRegistration( "Some device" ),
    MACAddressDeviceRegistration( MACAddress( "00-00-00-00-00-00" ) ),

    // Tasks.
    ConcurrentTask( "Start measures", listOf() ),
    CustomProtocolTask(
        "Custom study runtime",
        "{ \"\$type\": \"Study\", \"custom\": \"protocol\" }"
    ),

    // Measures.
    DataTypeMeasure( "dk.cachet.carp", "SomeType" ),
    PhoneSensorMeasure.geolocation(),

    // Triggers.
    ElapsedTimeTrigger( Smartphone( "User's phone" ), TimeSpan( 0 ) ),
    ManualTrigger(
        "User's phone",
        "Mood",
        "Describe how you are feeling at the moment."
    ),
    ScheduledTrigger(
        Smartphone( "User's phone"),
        TimeOfDay( 12 ), RecurrenceRule( RecurrenceRule.Frequency.DAILY )
    )
)

/**
 * Serialization tests for all extending types from base classes in [dk.cachet.carp.protocols].
 */
class SerializationTest : ConcreteTypesSerializationTest(
    createProtocolsSerializer(),
    PROTOCOLS_SERIAL_MODULE,
    protocolInstances )
