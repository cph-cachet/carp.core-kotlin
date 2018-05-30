package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [CustomMasterDeviceDescriptor].
 */
class CustomMasterDeviceDescriptorTest
{
    @Test
    fun `initialization from json extracts base MasterDeviceDescriptor properties`() {
        val device = UnknownMasterDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( device )

        val custom = CustomMasterDeviceDescriptor( UnknownMasterDeviceDescriptor::class.qualifiedName!!, serialized )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    private data class IncorrectMasterDevice( val incorrect: String = "Not a master device." )

    @Test
    fun `initialization from invalid json fails`()
    {
        val incorrect = IncorrectMasterDevice()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomMasterDeviceDescriptor( IncorrectMasterDevice::class.qualifiedName!!, serialized )
        }
    }
}