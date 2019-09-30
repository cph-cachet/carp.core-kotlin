package dk.cachet.carp.client.application

import dk.cachet.carp.client.domain.StudyRuntime
import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.protocols.domain.devices.*


typealias SmartphoneManager = ClientManager<Smartphone, DefaultDeviceRegistration>

/**
 * Create an application service which allows managing [StudyRuntime]'s on a smartphone.
 */
fun createSmartphoneManager(
    /**
     * The application service through which study deployments can be managed and retrieved.
     * Use [registrationBuilder] to configure or override default registration options.
     */
    deploymentManager: DeploymentManager,
    registrationBuilder: DefaultDeviceRegistrationBuilder.() -> Unit = {} ): SmartphoneManager
{
    val registration: DefaultDeviceRegistration = DefaultDeviceRegistrationBuilder().apply( registrationBuilder ).build()
    return SmartphoneManager( registration, deploymentManager )
}