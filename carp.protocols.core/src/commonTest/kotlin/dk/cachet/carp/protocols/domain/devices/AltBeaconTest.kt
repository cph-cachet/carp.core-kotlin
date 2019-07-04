package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
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
        val correctConfiguration = AltBeaconDeviceRegistration()
        val invalidConfiguration = DefaultDeviceRegistration( "id" )

        assertEquals( Trilean.TRUE, beacon.isValidConfiguration( correctConfiguration ) )
        assertEquals( Trilean.FALSE, beacon.isValidConfiguration( invalidConfiguration ) )
    }
}