package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.UUID
import kotlin.test.*


/**
 * Tests for [AltBeaconDeviceRegistration].
 */
class AltBeaconDeviceRegistrationTest
{
    @Test
    fun deviceId_is_unique()
    {
        val registration1 = AltBeaconDeviceRegistration(
            0, UUID( "00000000-0000-0000-0000-000000000000" ),
            1, 1 )
        val registration2 = AltBeaconDeviceRegistration(
            0, UUID( "00000000-0000-0000-0000-000000000000" ),
            1, 2 )

        assertNotEquals( registration1.deviceId, registration2.deviceId )
    }
}