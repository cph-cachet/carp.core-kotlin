package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeList
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import dk.cachet.carp.common.application.tasks.TaskDescriptorList
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A master device which uses a single [CustomProtocolTask] to determine how to run a study on the device.
 */
@Serializable
data class CustomProtocolDevice( override val roleName: String ) :
    MasterDeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    object Sensors : DataTypeSamplingSchemeList()
    object Tasks : TaskDescriptorList()


    // Measures and data types are defined in the custom `CustomProtocolTask.studyProtocol` and thus not managed by core.
    override fun getSupportedDataTypes(): Set<DataType> = Sensors.map { it.type }.toSet()

    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidConfiguration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
