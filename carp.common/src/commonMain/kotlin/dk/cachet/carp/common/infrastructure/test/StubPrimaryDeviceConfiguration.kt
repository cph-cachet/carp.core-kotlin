package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistrationBuilder
import dk.cachet.carp.common.application.devices.PrimaryDeviceConfiguration
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * A stub [PrimaryDeviceConfiguration] which can measure [STUB_DATA_TYPE].
 */
@Serializable
data class StubPrimaryDeviceConfiguration(
    @Required override val roleName: String = "Stub primary device",
    override val isOptional: Boolean = false,
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()
) : PrimaryDeviceConfiguration<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    object Sensors : DataTypeSamplingSchemeMap()
    {
        val STUB_DATA = add( StubDataTypeSamplingScheme() )
    }

    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors
    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidRegistration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
