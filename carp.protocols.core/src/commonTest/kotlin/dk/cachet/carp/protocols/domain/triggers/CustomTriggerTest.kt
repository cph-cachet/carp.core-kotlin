package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [CustomTrigger].
 */
class CustomTriggerTest
{
    @Test
    fun initialization_from_json_extracts_base_Trigger_properties() {
        val trigger = UnknownTrigger( "Some device" )
        val serialized: String = Json.stringify( UnknownTrigger.serializer(), trigger )

        val custom = CustomTrigger( "Irrelevant", serialized )
        assertEquals( trigger.sourceDeviceRoleName, custom.sourceDeviceRoleName )
    }

    @Serializable
    internal data class IncorrectTrigger( val incorrect: String = "Not a trigger." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectTrigger()
        val serialized: String = Json.stringify( IncorrectTrigger.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTrigger( "Irrelevant", serialized )
        }
    }
}