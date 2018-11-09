package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.UnknownDeviceRegistration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import kotlin.test.*


/**
 * Tests for [CustomDeviceRegistration].
 */
class CustomDeviceRegistrationTest
{
    @Test
    fun initialization_from_json_extracts_base_DeviceRegistration_properties()
    {
        val registration = UnknownDeviceRegistration( "Unknown" )
        val serialized: String = JSON.stringify( registration )

        val custom = CustomDeviceRegistration( "Irrelevant", serialized )
        assertEquals( registration.deviceId, custom.deviceId )
    }

    @Serializable
    internal data class IncorrectRegistration( val incorrect: String = "Not a registration." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectRegistration()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceRegistration( "Irrelevant", serialized )
        }
    }
}