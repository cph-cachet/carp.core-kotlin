package dk.cachet.carp.protocols.domain.devices

import kotlin.test.Test
import kotlin.test.assertEquals

class BLEHeartRateTest {
    @Test
    fun registration_builder_sets_properties() {
        val registration = BLEDeviceRegistrationBuilder().apply {
            deviceId = "myid"
            macAddress = "12.34.56.78.ab.cd"
            deviceName = "myname"
        }.build()

        assertEquals("myid", registration.deviceId)
        assertEquals("12.34.56.78.ab.cd", registration.macAddress)
        assertEquals("myname", registration.deviceName)
    }
}
