package dk.cachet.carp.common.application.devices

import kotlin.test.*


/**
 * Tests for [DefaultDeviceRegistration].
 */
class DefaultDeviceRegistrationTest
{
    @Test
    fun builder_sets_properties()
    {
        val deviceId = "Custom ID"
        val deviceDisplayName = "Device name"

        val registration = DefaultDeviceRegistrationBuilder().apply {
            this.deviceId = deviceId
            this.deviceDisplayName = deviceDisplayName
        }.build()

        assertEquals( deviceId, registration.deviceId )
        assertEquals( deviceDisplayName, registration.deviceDisplayName )
    }
}
