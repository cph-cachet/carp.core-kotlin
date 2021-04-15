package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.tasks.measures.Measure
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.infrastructure.serialization.COMMON_SERIAL_MODULE
import dk.cachet.carp.test.serialization.verifyTypesAreRegistered
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.*


@ExperimentalSerializationApi
class SerializationReflectionTest
{
    @Test
    fun all_DeviceDescriptor_types_registered_for_serialization() =
        verifyTypesAreRegistered<AnyDeviceDescriptor>( COMMON_SERIAL_MODULE )

    @Test
    fun all_SamplingConfiguration_types_registered_for_serialization() =
        verifyTypesAreRegistered<SamplingConfiguration>( COMMON_SERIAL_MODULE )

    @Test
    fun all_DeviceRegistration_types_registered_for_serialization() =
        verifyTypesAreRegistered<DeviceRegistration>( COMMON_SERIAL_MODULE )

    @Test
    fun all_TaskDescriptor_types_registered_for_serialization() =
        verifyTypesAreRegistered<TaskDescriptor>( COMMON_SERIAL_MODULE )

    @Test
    fun all_Measure_types_registered_for_serialization() =
        verifyTypesAreRegistered<Measure>( COMMON_SERIAL_MODULE )

    @Test
    fun all_Trigger_types_registered_for_serialization() =
        verifyTypesAreRegistered<Trigger>( COMMON_SERIAL_MODULE )
}
