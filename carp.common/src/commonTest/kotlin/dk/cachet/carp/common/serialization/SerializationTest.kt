@file:Suppress( "WildcardImport" )

package dk.cachet.carp.common.serialization

import dk.cachet.carp.common.data.*
import dk.cachet.carp.common.data.input.element.*
import dk.cachet.carp.common.users.*
import dk.cachet.carp.test.serialization.ConcreteTypesSerializationTest
import kotlinx.serialization.PolymorphicSerializer
import kotlin.test.*


private val commonInstances = listOf(
    // Account identities.
    EmailAccountIdentity( "test@test.com" ),
    UsernameAccountIdentity( "Some user" ),

    // Data objects.
    Acceleration( 42.0, 42.0, 42.0 ),
    ECG( 42.0 ),
    FreeFormText( "Some text" ),
    Geolocation( 42.0, 42.0 ),
    HeartRate( 60 ),
    RRInterval,
    SensorSkinContact( true ),
    StepCount( 42 ),

    // Input elements.
    SelectOne( "Sex", setOf( "Male", "Female" ) ),
    Text( "Name" )
)

/**
 * Serialization tests for all extending types from base classes in [dk.cachet.carp.common].
 */
class SerializationTest : ConcreteTypesSerializationTest(
    createDefaultJSON(),
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
}
