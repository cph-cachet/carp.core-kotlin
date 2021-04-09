@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomSamplingConfiguration].
 */
class CustomSamplingConfigurationTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Serializable
    class SomeRandomSerializableObject( val noMatchingBaseProperties: String )

    @Test
    fun initialization_from_any_object_succeeds()
    {
        val anyObject = SomeRandomSerializableObject( "Whatever" )
        val serialized: String = JSON.encodeToString( SomeRandomSerializableObject.serializer(), anyObject )

        // Currently, there are no base members in `SamplingConfiguration`.
        // Any class, even those that don't implement `SamplingConfiguration` can be used.
        val custom = CustomSamplingConfiguration( "Irrelevant", serialized, JSON )
        assertTrue( custom.jsonSource.contains( "Whatever" ) )
    }
}
