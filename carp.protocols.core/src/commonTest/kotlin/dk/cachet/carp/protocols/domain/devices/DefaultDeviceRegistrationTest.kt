package dk.cachet.carp.protocols.domain.devices

import kotlin.test.*


/**
 * Tests for [DefaultDeviceRegistration].
 */
class DefaultDeviceRegistrationTest
{
    @Test
    fun builder_sets_deviceId()
    {
        val registration = DefaultDeviceRegistrationBuilder( "Default ID" ).apply {
            deviceId { "Custom ID" }
        }.build()

        assertEquals( "Custom ID", registration.deviceId )
    }
}