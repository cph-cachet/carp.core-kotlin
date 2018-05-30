package dk.cachet.carp.protocols.domain.triggers

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [CustomTrigger].
 */
class CustomTriggerTest
{
    @Test
    fun `initialization from json extracts base Trigger  properties`() {
        val trigger = UnknownTrigger( "Some device" )
        val serialized: String = JSON.stringify( trigger )

        val custom = CustomTrigger( UnknownTrigger::class.qualifiedName!!, serialized )
        assertEquals( trigger.sourceDeviceRoleName, custom.sourceDeviceRoleName )
    }

    @Serializable
    private data class IncorrectTrigger( val incorrect: String = "Not a trigger." )

    @Test
    fun `initialization from invalid json fails`()
    {
        val incorrect = IncorrectTrigger()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTrigger( IncorrectTrigger::class.qualifiedName!!, serialized )
        }
    }
}