package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [CustomDeviceDescriptor].
 */
class CustomDeviceDescriptorTest
{
    @Test
    fun `initialization from json extracts base DeviceDescriptor properties`() {
        val device = UnknownDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( device )

        val custom = CustomDeviceDescriptor( UnknownDeviceDescriptor::class.qualifiedName!!, serialized )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    private data class IncorrectDevice( val incorrect: String = "Not a device." )

    @Test
    fun `initialization from invalid json fails`()
    {
        val incorrect = IncorrectDevice()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceDescriptor( IncorrectDevice::class.qualifiedName!!, serialized )
        }
    }
}