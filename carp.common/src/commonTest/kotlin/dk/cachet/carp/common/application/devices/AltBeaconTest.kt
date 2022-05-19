package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
            1, 1, 0
        )
        val registration2 = AltBeaconDeviceRegistration(
            0, UUID( "00000000-0000-0000-0000-000000000000" ),
            1, 2, 0
        )

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
            deviceDisplayName = "AltBeacon"
        }.build()

        assertEquals( 1, registration.manufacturerId )
        assertEquals( UUID( "00000000-0000-0000-0000-000000000002" ), registration.organizationId )
        assertEquals( 3, registration.majorId )
        assertEquals( 4, registration.minorId )
        assertEquals( "AltBeacon", registration.deviceDisplayName )
    }

    @Test
    fun registration_deviceId_and_deviceDisplayName_is_serialized()
    {
        val registration = AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0, 0, "Device" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( AltBeaconDeviceRegistration.serializer(), registration )
        val jsonElement = json.parseToJsonElement( serialized ).jsonObject
        val serializedDeviceId = jsonElement[ DeviceRegistration::deviceId.name ]?.jsonPrimitive?.content
        assertEquals( registration.deviceId, serializedDeviceId )
        val serializedDeviceDisplayName = jsonElement[ DeviceRegistration::deviceDisplayName.name ]?.jsonPrimitive?.content
        assertEquals( registration.deviceDisplayName, serializedDeviceDisplayName )
    }
}
