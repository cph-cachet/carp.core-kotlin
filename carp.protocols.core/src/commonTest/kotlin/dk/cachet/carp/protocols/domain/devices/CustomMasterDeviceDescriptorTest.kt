package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomMasterDeviceDescriptor].
 */
class CustomMasterDeviceDescriptorTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_MasterDeviceDescriptor_properties()
    {
        val device = UnknownMasterDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( UnknownMasterDeviceDescriptor.serializer(), device )

        val custom = CustomMasterDeviceDescriptor( "Irrelevant", serialized, JSON )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectMasterDevice( val incorrect: String = "Not a master device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectMasterDevice()
        val serialized: String = JSON.stringify( IncorrectMasterDevice.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomMasterDeviceDescriptor( "Irrelevant", serialized, JSON )
        }
    }

    @Test
    fun createRegistration_is_not_supported()
    {
        val device = UnknownMasterDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.stringify( UnknownMasterDeviceDescriptor.serializer(), device )

        val custom = CustomMasterDeviceDescriptor( "Irrelevant", serialized, JSON )

        assertFailsWith<UnsupportedOperationException>
        {
            custom.createRegistration()
        }
    }
}