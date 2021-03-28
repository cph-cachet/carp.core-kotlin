@file:Suppress( "WildcardImport" )

package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.MACAddress
import dk.cachet.carp.common.application.RecurrenceRule
import dk.cachet.carp.common.application.TimeOfDay
import dk.cachet.carp.common.application.TimeSpan
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.sampling.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import dk.cachet.carp.protocols.domain.triggers.*
import dk.cachet.carp.protocols.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMeasure
import dk.cachet.carp.protocols.infrastructure.test.StubSamplingConfiguration
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTrigger
import dk.cachet.carp.test.serialization.ConcreteTypesSerializationTest
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


const val testClassDiscriminator = "_type"
val testJson = Json( createProtocolsSerializer( STUBS_SERIAL_MODULE ) ) { classDiscriminator = testClassDiscriminator }

private val protocolInstances = listOf(
    // Devices.
    AltBeacon( "Kitchen" ),
    BLEHeartRateSensor( "Polar" ),
    CustomProtocolDevice( "User's phone" ),
    Smartphone( "User's phone" ),
    unknown( StubDeviceDescriptor() ) { CustomDeviceDescriptor( it.first, it.second, it.third ) },
    unknown( StubMasterDeviceDescriptor() ) { CustomMasterDeviceDescriptor( it.first, it.second, it.third ) },

    // Sampling configurations.
    IntervalSamplingConfiguration( TimeSpan.fromMilliseconds( 1000.0 ) ),
    NoOptionsSamplingConfiguration,
    unknown( StubSamplingConfiguration( "" ) ) { CustomSamplingConfiguration( it.first, it.second, it.third ) },

    // Device registrations.
    AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0 ),
    BLESerialNumberDeviceRegistration( "123456789" ),
    DefaultDeviceRegistration( "Some device" ),
    MACAddressDeviceRegistration( MACAddress( "00-00-00-00-00-00" ) ),
    unknown( DefaultDeviceRegistration( "id" ) ) { CustomDeviceRegistration( it.first, it.second, it.third ) },

    // Tasks.
    ConcurrentTask( "Start measures", listOf() ),
    CustomProtocolTask(
        "Custom study runtime",
        "{ \"\$type\": \"Study\", \"custom\": \"protocol\" }"
    ),
    unknown( StubTaskDescriptor() ) { CustomTaskDescriptor( it.first, it.second, it.third ) },

    // Measures.
    DataTypeMeasure( "dk.cachet.carp", "SomeType" ),
    PhoneSensorMeasure.geolocation(),
    unknown( StubMeasure() ) { CustomMeasure( it.first, it.second, it.third ) },

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
    ),
    unknown( StubTrigger( "source" ) ) { CustomTrigger( it.first, it.second, it.third ) }
)

/**
 * Convert the specified [stub] to the corresponding [UnknownPolymorphicWrapper] as if it were unknown at runtime.
 */
inline fun <reified Base : Any> unknown( stub: Base, constructor: (Triple<String, String, Json>) -> Base ): Base
{
    val originalObject = testJson.encodeToJsonElement( PolymorphicSerializer( Base::class ), stub ) as JsonObject

    // Mimic the passed stub as an unknown object.
    val unknownName = "Unknown"
    val unknownObject = originalObject.toMutableMap()
    unknownObject[ testClassDiscriminator ] = JsonPrimitive( unknownName )
    val jsonSource = testJson.encodeToString( unknownObject )

    return constructor( Triple( unknownName, jsonSource, testJson ) )
}

/**
 * Serialization tests for all extending types from base classes in [dk.cachet.carp.protocols].
 */
class SerializationTest : ConcreteTypesSerializationTest(
    testJson,
    PROTOCOLS_SERIAL_MODULE,
    protocolInstances )
