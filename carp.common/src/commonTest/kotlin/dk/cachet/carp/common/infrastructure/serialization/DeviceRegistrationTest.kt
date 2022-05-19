package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [DeviceRegistration] relying on core infrastructure.
 */
class DeviceRegistrationTest
{
    @Test
    fun can_serialize_and_deserialize_device_registration_using_JSON()
    {
        val default: DeviceRegistration = DefaultDeviceRegistration()

        val serialized = testJson.encodeToString( DeviceRegistration.serializer(), default )
        val parsed: DeviceRegistration = testJson.decodeFromString( serialized )

        assertEquals( default, parsed )
    }

    /**
     * Types not known at compile time should not prevent deserializing a device registration, but should be loaded through a 'Custom' type wrapper.
     */
    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val unknownRegistration = serializeUnknownDeviceRegistration()
        val parsed: DeviceRegistration = testJson.decodeFromString( unknownRegistration )

        assertTrue( parsed is CustomDeviceRegistration )
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val unknownRegistration = serializeUnknownDeviceRegistration()
        val parsed: DeviceRegistration = testJson.decodeFromString( unknownRegistration )

        val serialized = testJson.encodeToString( parsed )
        assertEquals( unknownRegistration, serialized )
    }

    private fun serializeUnknownDeviceRegistration(): String
    {
        val registration = DefaultDeviceRegistration()
        var serialized = testJson.encodeToString( DeviceRegistration.serializer(), registration )
        serialized = serialized.replace( "dk.cachet.carp.common.application.devices.DefaultDeviceRegistration", "com.unknown.CustomRegistration" )

        return serialized
    }
}
