package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.ConnectedDeviceDataCollector
import dk.cachet.carp.client.domain.data.DeviceDataCollector
import dk.cachet.carp.client.domain.data.DeviceDataCollectorFactory
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.devices.SmartphoneDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.SmartphoneDeviceRegistrationBuilder


/**
 * Allows managing [StudyRuntime]s on a smartphone.
 */
class SmartphoneClient(
    /**
     * Repository within which the state of this client is stored.
     */
    repository: ClientRepository,
    /**
     * The application service through which study deployments, to be run on this client, can be managed and retrieved.
     */
    deploymentService: DeploymentService,
    /**
     * Determines which [DeviceDataCollector] to use to collect data locally on this master device
     * and this factory is used to create [ConnectedDeviceDataCollector] instances for connected devices.
     */
    dataCollectorFactory: DeviceDataCollectorFactory
) :
ClientManager<Smartphone, SmartphoneDeviceRegistration, SmartphoneDeviceRegistrationBuilder>(
    repository,
    deploymentService,
    dataCollectorFactory
)
{
    override fun createDeviceRegistrationBuilder(): SmartphoneDeviceRegistrationBuilder = SmartphoneDeviceRegistrationBuilder()
}
