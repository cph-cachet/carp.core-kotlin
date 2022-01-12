package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
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
        val device = StubDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.encodeToString( StubDeviceDescriptor.serializer(), device )

        val custom = CustomDeviceDescriptor( "Irrelevant", serialized, JSON )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectDevice( val incorrect: String = "Not a device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectDevice()
        val serialized: String = JSON.encodeToString( IncorrectDevice.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceDescriptor( "Irrelevant", serialized, JSON )
        }
    }

    @Test
    fun createRegistration_is_not_supported()
    {
        val device = StubDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.encodeToString( StubDeviceDescriptor.serializer(), device )

        val custom = CustomDeviceDescriptor( "Irrelevant", serialized, JSON )

        assertFailsWith<UnsupportedOperationException>
        {
            custom.createRegistration()
        }
    }
}


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
        val device = StubMasterDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.encodeToString( StubMasterDeviceDescriptor.serializer(), device )

        val custom = CustomMasterDeviceDescriptor( "Irrelevant", serialized, JSON )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectMasterDevice( val incorrect: String = "Not a master device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectMasterDevice()
        val serialized: String = JSON.encodeToString( IncorrectMasterDevice.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomMasterDeviceDescriptor( "Irrelevant", serialized, JSON )
        }
    }

    @Test
    fun createRegistration_is_not_supported()
    {
        val device = StubMasterDeviceDescriptor( "Unknown" )
        val serialized: String = JSON.encodeToString( StubMasterDeviceDescriptor.serializer(), device )

        val custom = CustomMasterDeviceDescriptor( "Irrelevant", serialized, JSON )

        assertFailsWith<UnsupportedOperationException>
        {
            custom.createRegistration()
        }
    }
}


/**
 * Tests for [CustomDeviceRegistration].
 */
class CustomDeviceRegistrationTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_DeviceRegistration_properties()
    {
        val registration = DefaultDeviceRegistration()
        val serialized: String = JSON.encodeToString( DefaultDeviceRegistration.serializer(), registration )

        val custom = CustomDeviceRegistration( "Irrelevant", serialized, JSON )
        assertEquals( registration.deviceId, custom.deviceId )
        assertEquals( registration.deviceDisplayName, custom.deviceDisplayName )
    }

    @Serializable
    internal data class IncorrectRegistration( val incorrect: String = "Not a registration." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectRegistration()
        val serialized: String = JSON.encodeToString( IncorrectRegistration.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceRegistration( "Irrelevant", serialized, JSON )
        }
    }
}
