package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.UUID
import kotlin.test.*


/**
 * Tests for [AltBeacon] and [AltBeaconDeviceRegistration].
 */
class AltBeaconTest
{
    @Test
    fun registration_deviceId_is_unique()
    {
        val registration1 = AltBeaconDeviceRegistration(
                0, UUID( "00000000-0000-0000-0000-000000000000" ),
                1, 1 )
        val registration2 = AltBeaconDeviceRegistration(
                0, UUID( "00000000-0000-0000-0000-000000000000" ),
                1, 2 )

        assertNotEquals( registration1.deviceId, registration2.deviceId )
    }

    @Test
    fun registration_builder_sets_properties()
    {
        val registration = AltBeaconDeviceRegistrationBuilder().apply {
            manufacturerId = 1
            organizationId = UUID( "00000000-0000-0000-0000-000000000002" )
            majorId = 3
            minorId = 4
        }.build()

        assertEquals( 1, registration.manufacturerId )
        assertEquals( UUID( "00000000-0000-0000-0000-000000000002" ), registration.organizationId )
        assertEquals( 3, registration.majorId )
        assertEquals( 4, registration.minorId )
    }
}
