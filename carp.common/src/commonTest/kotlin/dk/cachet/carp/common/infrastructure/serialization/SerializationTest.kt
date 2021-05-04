@file:Suppress( "WildcardImport" )

package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.*
import dk.cachet.carp.common.application.data.*
import dk.cachet.carp.common.application.data.input.*
import dk.cachet.carp.common.application.data.input.elements.*
import dk.cachet.carp.common.application.devices.*
import dk.cachet.carp.common.application.sampling.*
import dk.cachet.carp.common.application.tasks.*
import dk.cachet.carp.common.application.triggers.*
import dk.cachet.carp.common.application.users.*
import dk.cachet.carp.common.infrastructure.test.*
import dk.cachet.carp.test.serialization.ConcreteTypesSerializationTest
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.*


val testJson = createDefaultJSON( STUBS_SERIAL_MODULE )

/**
 * Convert the specified [stub] to the corresponding [UnknownPolymorphicWrapper] as if it were unknown at runtime.
 */
inline fun <reified Base : Any> unknown( stub: Base, constructor: (Triple<String, String, Json>) -> Base ): Base
{
    val originalObject = testJson.encodeToJsonElement( PolymorphicSerializer( Base::class ), stub ) as JsonObject

    // Mimic the passed stub as an unknown object.
    val unknownName = "Unknown"
    val unknownObject = originalObject.toMutableMap()
    unknownObject[ CLASS_DISCRIMINATOR ] = JsonPrimitive( unknownName )
    val jsonSource = testJson.encodeToString( unknownObject )

    return constructor( Triple( unknownName, jsonSource, testJson ) )
}

private val commonInstances = listOf(
    // `data` namespace.
    Acceleration( 42.0, 42.0, 42.0 ),
    ECG( 42.0 ),
    FreeFormText( "Some text" ),
    Geolocation( 42.0, 42.0 ),
    HeartRate( 60 ),
    RRInterval,
    SensorSkinContact( true ),
    StepCount( 42 ),

    // `data.input` namespace.
    CustomInput( "42" ),
    Sex.Male,

    // `data.input.elements` namespace.
    SelectOne( "Sex", setOf( "Male", "Female" ) ),
    Text( "Name" ),

    // Devices in `devices` namespace.
    AltBeacon( "Kitchen" ),
    AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0 ),
    BLEHeartRateDevice( "Polar" ),
    BLESerialNumberDeviceRegistration( "123456789" ),
    CustomProtocolDevice( "User's phone" ),
    Smartphone( "User's phone" ),

    // Shared device registrations in `devices` namespace.
    DefaultDeviceRegistration( "Some device" ),
    MACAddressDeviceRegistration( MACAddress( "00-00-00-00-00-00" ) ),

    // `sampling` namespace.
    GranularitySamplingConfiguration( Granularity.Balanced ),
    IntervalSamplingConfiguration( TimeSpan.fromMilliseconds( 1000.0 ) ),
    NoOptionsSamplingConfiguration,

    // `tasks` namespace.
    BackgroundTask( "Start measures", listOf() ),
    CustomProtocolTask(
        "Custom study runtime",
        "{ \"\$type\": \"Study\", \"custom\": \"protocol\" }"
    ),

    // `triggers` namespace.
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

    // `users` namespace.
    EmailAccountIdentity( "test@test.com" ),
    UsernameAccountIdentity( "Some user" ),

    // `infrastructure` namespace.
    unknown( StubDeviceDescriptor() ) { CustomDeviceDescriptor( it.first, it.second, it.third ) },
    unknown( StubMasterDeviceDescriptor() ) { CustomMasterDeviceDescriptor( it.first, it.second, it.third ) },
    unknown( DefaultDeviceRegistration( "id" ) ) { CustomDeviceRegistration( it.first, it.second, it.third ) },
    unknown( StubSamplingConfiguration( "" ) ) { CustomSamplingConfiguration( it.first, it.second, it.third ) },
    unknown( StubTaskDescriptor() ) { CustomTaskDescriptor( it.first, it.second, it.third ) },
    unknown( StubTrigger( "source" ) ) { CustomTrigger( it.first, it.second, it.third ) },
)


/**
 * Serialization tests for all extending types from base classes in [dk.cachet.carp.common].
 */
class SerializationTest : ConcreteTypesSerializationTest(
    testJson,
    COMMON_SERIAL_MODULE,
    commonInstances )
{
    @Test
    fun can_serialize_and_deserialize_polymorphic_InputElement()
    {
        val inputElement: InputElement<*> = Text( "Test" )
        val polySerializer = PolymorphicSerializer( InputElement::class )
        val serialized = json.encodeToString( polySerializer, inputElement )
        val parsed = json.decodeFromString( polySerializer, serialized )
        assertEquals( inputElement, parsed )
    }

    @Test
    fun can_serialize_generic_CustomInput()
    {
        val data: Data = CustomInput( 42 )
        val dataSerializer = PolymorphicSerializer( Data::class )

        val serialized = json.encodeToString( dataSerializer, data )
        val parsed = json.decodeFromString( dataSerializer, serialized )
        assertEquals( data, parsed )
    }
}
