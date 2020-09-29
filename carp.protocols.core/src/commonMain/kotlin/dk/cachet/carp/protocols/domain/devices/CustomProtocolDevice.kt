package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import dk.cachet.carp.protocols.domain.tasks.CustomProtocolTask
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A master device which uses a single [CustomProtocolTask] to determine how to run a study on the device.
 */
@Serializable
data class CustomProtocolDevice( override val roleName: String ) :
    MasterDeviceDescriptor<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    // Measures and data types are defined in the custom `CustomProtocolTask.studyProtocol` and thus not managed by core.
    override val supportedDataTypes: Set<DataType> = emptySet()

    override val samplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidConfiguration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
