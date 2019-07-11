package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomDeviceDescriptor].
 */
class CustomDeviceDescriptorTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_DeviceDescriptor_properties()
    {
        val device = UnknownDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( UnknownDeviceDescriptor.serializer(), device )

        val custom = CustomDeviceDescriptor( "Irrelevant", serialized, JSON )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectDevice( val incorrect: String = "Not a device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectDevice()
        val serialized: String = JSON.stringify( IncorrectDevice.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceDescriptor( "Irrelevant", serialized, JSON )
        }
    }

    @Test
    fun createRegistration_is_not_supported()
    {
        val device = UnknownDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( UnknownDeviceDescriptor.serializer(), device )

        val custom = CustomDeviceDescriptor( "Irrelevant", serialized, JSON )

        assertFailsWith<UnsupportedOperationException>
        {
            custom.createRegistration()
        }
    }
}