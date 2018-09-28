package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.json.JSON
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [CustomTrigger].
 */
@JsIgnore
class CustomTriggerTest
{
    @Test
    fun initialization_from_json_extracts_base_Trigger_properties() {
        val trigger = UnknownTrigger( "Some device" )
        val serialized: String = JSON.stringify( trigger )

        val custom = CustomTrigger( "Irrelevant", serialized )
        assertEquals( trigger.sourceDeviceRoleName, custom.sourceDeviceRoleName )
    }

    @Serializable
    private data class IncorrectTrigger( val incorrect: String = "Not a trigger." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectTrigger()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTrigger( "Irrelevant", serialized )
        }
    }
}