package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.json.JSON
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [CustomDeviceDescriptor].
 */
class CustomDeviceDescriptorTest
{
    @Test
    fun initialization_from_json_extracts_base_DeviceDescriptor_properties()
    {
        val device = UnknownDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( device )

        val custom = CustomDeviceDescriptor( "Irrelevant", serialized )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectDevice( val incorrect: String = "Not a device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectDevice()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceDescriptor( "Irrelevant", serialized )
        }
    }
}