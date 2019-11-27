package dk.cachet.carp.client.domain

import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistrationBuilder
import dk.cachet.carp.protocols.domain.devices.Smartphone


typealias SmartphoneManager = ClientManager<Smartphone, DefaultDeviceRegistration>

/**
 * Create an application service which allows managing [StudyRuntime]'s on a smartphone.
 */
fun createSmartphoneManager(
    /**
     * The application service through which study deployments can be managed and retrieved.
     * Use [registrationBuilder] to configure or override default registration options.
     */
    deploymentService: DeploymentService,
    registrationBuilder: DefaultDeviceRegistrationBuilder.() -> Unit = {} ): SmartphoneManager
{
    val registration: DefaultDeviceRegistration = DefaultDeviceRegistrationBuilder().apply( registrationBuilder ).build()
    return SmartphoneManager( registration, deploymentService )
}
