package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.json.JSON
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [CustomMasterDeviceDescriptor].
 */
class CustomMasterDeviceDescriptorTest
{
    @Test
    fun initialization_from_json_extracts_base_MasterDeviceDescriptor_properties()
    {
        val device = UnknownMasterDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( device )

        val custom = CustomMasterDeviceDescriptor( "Irrelevant", serialized )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectMasterDevice( val incorrect: String = "Not a master device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectMasterDevice()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomMasterDeviceDescriptor( "Irrelevant", serialized )
        }
    }
}