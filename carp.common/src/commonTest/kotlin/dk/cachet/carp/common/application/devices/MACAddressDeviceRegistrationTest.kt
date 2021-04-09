package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.MACAddress
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*


/**
 * Tests for [MACAddressDeviceRegistration].
 */
class MACAddressDeviceRegistrationTest
{
    @Test
    fun builder_sets_deviceId()
    {
        val mac = "00-11-22-33-44-55"
        val registration = MACAddressDeviceRegistrationBuilder().apply {
            macAddress = mac
        }.build()

        assertEquals( mac, registration.deviceId )
    }

    @Test
    fun deviceId_is_serialized()
    {
        val mac = "00-11-22-33-44-55"
        val registration = MACAddressDeviceRegistration( MACAddress( mac ) )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( MACAddressDeviceRegistration.serializer(), registration )
        val jsonElement = json.parseToJsonElement( serialized ).jsonObject
        val serializedDeviceId = jsonElement[ DeviceRegistration::deviceId.name ]?.jsonPrimitive?.content
        assertEquals( registration.deviceId, serializedDeviceId )
    }
}
