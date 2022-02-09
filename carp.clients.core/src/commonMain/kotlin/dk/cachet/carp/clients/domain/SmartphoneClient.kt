package dk.cachet.carp.clients.domain

import dk.cachet.carp.clients.application.ClientManager
import dk.cachet.carp.clients.domain.data.ConnectedDeviceDataCollector
import dk.cachet.carp.clients.domain.data.DeviceDataCollector
import dk.cachet.carp.clients.domain.data.DeviceDataCollectorFactory
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.devices.SmartphoneDeviceRegistration
import dk.cachet.carp.common.application.devices.SmartphoneDeviceRegistrationBuilder
import dk.cachet.carp.deployments.application.DeploymentService


/**
 * Allows managing studies on a smartphone.
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
