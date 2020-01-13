package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.CustomDeviceRegistration
import kotlin.test.*


/**
 * Tests for [DeviceRegistration] relying on core infrastructure.
 */
class DeviceRegistrationTest
{
    @Test
    fun can_serialize_and_deserialize_device_registration_using_JSON()
    {
        val default: DeviceRegistration = DefaultDeviceRegistration( "Test" )

        val serialized = default.toJson()
        val parsed = DeviceRegistration.fromJson( serialized )

        assertEquals( default, parsed )
    }

    /**
     * Types not known at compile time should not prevent deserializing a device registration, but should be loaded through a 'Custom' type wrapper.
     */
    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val unknownRegistration = serializeUnknownDeviceRegistration()
        val parsed = DeviceRegistration.fromJson( unknownRegistration )

        assertTrue( parsed is CustomDeviceRegistration )
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val unknownRegistration = serializeUnknownDeviceRegistration()
        val parsed = DeviceRegistration.fromJson( unknownRegistration )

        val serialized = parsed.toJson()
        assertEquals( unknownRegistration, serialized )
    }

    fun serializeUnknownDeviceRegistration(): String
    {
        val registration = DefaultDeviceRegistration( "Test" )
        var serialized = registration.toJson()
        serialized = serialized.replace( "dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration", "com.unknown.CustomRegistration" )

        return serialized
    }
}
