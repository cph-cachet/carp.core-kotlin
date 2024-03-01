@file:Suppress("NON_EXPORTABLE_TYPE")

package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfigurationList
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.reflect.KClass

typealias WebServerDeviceRegistration = DefaultDeviceRegistration
typealias WebServerDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder

@Serializable
@JsExport
data class WebServer(
        val url: String,
        override val roleName: String,
        override val isOptional: Boolean = false
) : PrimaryDeviceConfiguration<WebServerDeviceRegistration, WebServerDeviceRegistrationBuilder>()
{
    companion object
    {
        fun create( url: String, roleName: String ): WebServer = WebServer( url, roleName )
    }

    object Sensors : DataTypeSamplingSchemeMap()
    object Tasks : TaskConfigurationList()

    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys

    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors

    override fun createDeviceRegistrationBuilder(): WebServerDeviceRegistrationBuilder =
            WebServerDeviceRegistrationBuilder()

    override fun getRegistrationClass(): KClass<WebServerDeviceRegistration> = WebServerDeviceRegistration::class

    override fun isValidRegistration( registration: WebServerDeviceRegistration ): Trilean = Trilean.TRUE
}
