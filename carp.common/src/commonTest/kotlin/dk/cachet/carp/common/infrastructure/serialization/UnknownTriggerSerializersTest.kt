@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomTriggerConfiguration].
 */
class CustomTriggerTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_Trigger_properties()
    {
        val trigger = StubTriggerConfiguration( "Some device" )
        val serialized: String = JSON.encodeToString( StubTriggerConfiguration.serializer(), trigger )

        val custom = CustomTriggerConfiguration( "Irrelevant", serialized, JSON )
        assertEquals( trigger.sourceDeviceRoleName, custom.sourceDeviceRoleName )
    }

    @Serializable
    internal data class IncorrectTrigger( val incorrect: String = "Not a trigger." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectTrigger()
        val serialized: String = JSON.encodeToString( IncorrectTrigger.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTriggerConfiguration( "Irrelevant", serialized, JSON )
        }
    }
}
