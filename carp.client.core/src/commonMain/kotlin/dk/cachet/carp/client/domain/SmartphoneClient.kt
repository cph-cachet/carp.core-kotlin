package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.DataCollector
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.DataType
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
     * Manages [Data] collection of requested [DataType]s for this master device and connected devices.
     */
    dataCollector: DataCollector
) :
ClientManager<Smartphone, SmartphoneDeviceRegistration, SmartphoneDeviceRegistrationBuilder>(
    repository,
    deploymentService,
    dataCollector
)
{
    override fun createDeviceRegistrationBuilder(): SmartphoneDeviceRegistrationBuilder = SmartphoneDeviceRegistrationBuilder()
}
