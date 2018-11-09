package dk.cachet.carp.protocols.domain.devices

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


/**
 * Tests for [DeviceRegistration].
 */
class DeviceRegistrationTest
{
    @Test
    fun copy_succeeds()
    {
        val registration = DefaultDeviceRegistration( "0" )
        val copy = registration.copy()

        assertFalse { registration === copy }
        assertEquals( registration, copy )
    }
}