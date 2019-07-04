package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.UUID
import kotlin.test.*


/**
 * Tests for [AltBeacon].
 */
class AltBeaconTest
{
    @Test
    fun isValidConfiguration_expects_AltBeaconDeviceRegistration()
    {
        val beacon = AltBeacon( "beacon" )
        val correctConfiguration = AltBeaconDeviceRegistration( 0, UUID( "00000000-0000-0000-0000-000000000000" ), 0, 0 )
        val invalidConfiguration = DefaultDeviceRegistration( "id" )

        assertEquals( Trilean.TRUE, beacon.isValidConfiguration( correctConfiguration ) )
        assertEquals( Trilean.FALSE, beacon.isValidConfiguration( invalidConfiguration ) )
    }
}