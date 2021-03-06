package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [ApplicationDataSerializer].
 */
class ApplicationDataSerializerTest
{
    @Serializable
    data class ContainsApplicationData(
        val normalData: String,
        @Serializable( ApplicationDataSerializer::class )
        val applicationData: String
    )


    @Test
    fun can_serialize_and_deserialize_non_json_application_data()
    {
        val toSerialize = ContainsApplicationData( "normal", "some application data" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( toSerialize )
        val parsed: ContainsApplicationData = json.decodeFromString( serialized )
        assertEquals( toSerialize, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_json_application_data()
    {
        val json = createDefaultJSON()

        // Parse application data as JSON to remove newlines/white spaces.
        val applicationData = json.parseToJsonElement(
            """
            {
                "key": "Some value",
                "key2": {
                    "nested": "object"
                }
            }
            """
        )
        val toSerialize = ContainsApplicationData( "normal", applicationData.toString() )

        val serialized = json.encodeToString( toSerialize )
        val parsed: ContainsApplicationData = json.decodeFromString( serialized )

        assertEquals( toSerialize, parsed )
    }

    @Test
    fun json_serializer_serializes_as_json_element()
    {
        val toSerialize = ContainsApplicationData( "normal", """{"json":"data"}""" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( toSerialize )

        // The application data should not be escaped, but simply be added as plain JSON.
        assertFalse( serialized.contains( '\\' ) )
    }

    @Test
    fun can_serialize_malformed_json()
    {
        val malformedJson = """{"json object":"or not?"""
        val toSerialize = ContainsApplicationData( "normal", malformedJson )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( toSerialize )

        // Fallback for malformed JSON is serializing it as an escaped string.
        assertTrue( serialized.contains( '\\' ) )
    }
}
