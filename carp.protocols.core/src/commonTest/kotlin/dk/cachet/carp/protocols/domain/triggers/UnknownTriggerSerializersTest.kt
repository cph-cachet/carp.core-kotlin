package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.UnknownTrigger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomTrigger].
 */
class CustomTriggerTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_Trigger_properties() {
        val trigger = UnknownTrigger( "Some device" )
        val serialized: String = JSON.stringify( UnknownTrigger.serializer(), trigger )

        val custom = CustomTrigger( "Irrelevant", serialized, JSON )
        assertEquals( trigger.sourceDeviceRoleName, custom.sourceDeviceRoleName )
    }

    @Serializable
    internal data class IncorrectTrigger( val incorrect: String = "Not a trigger." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectTrigger()
        val serialized: String = JSON.stringify( IncorrectTrigger.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTrigger( "Irrelevant", serialized, JSON )
        }
    }
}