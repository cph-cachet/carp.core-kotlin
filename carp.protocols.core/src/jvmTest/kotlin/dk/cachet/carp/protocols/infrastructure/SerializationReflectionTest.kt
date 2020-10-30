package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.domain.triggers.Trigger
import dk.cachet.carp.test.serialization.verifyTypesAreRegistered
import kotlin.test.*


class SerializationReflectionTest
{
    @Test
    fun all_DeviceDescriptor_types_registered_for_serialization() =
        verifyTypesAreRegistered<AnyDeviceDescriptor>( PROTOCOLS_SERIAL_MODULE )

    @Test
    fun all_SamplingConfiguration_types_registered_for_serialization() =
        verifyTypesAreRegistered<SamplingConfiguration>( PROTOCOLS_SERIAL_MODULE )

    @Test
    fun all_DeviceRegistration_types_registered_for_serialization() =
        verifyTypesAreRegistered<DeviceRegistration>( PROTOCOLS_SERIAL_MODULE )

    @Test
    fun all_TaskDescriptor_types_registered_for_serialization() =
        verifyTypesAreRegistered<TaskDescriptor>( PROTOCOLS_SERIAL_MODULE )

    @Test
    fun all_Measure_types_registered_for_serialization() =
        verifyTypesAreRegistered<Measure>( PROTOCOLS_SERIAL_MODULE )

    @Test
    fun all_Trigger_types_registered_for_serialization() =
        verifyTypesAreRegistered<Trigger>( PROTOCOLS_SERIAL_MODULE )
}
