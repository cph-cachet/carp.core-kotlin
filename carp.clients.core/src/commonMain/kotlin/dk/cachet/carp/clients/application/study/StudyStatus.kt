package dk.cachet.carp.clients.application.study

import dk.cachet.carp.clients.domain.DeviceRegistrationStatus
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus


/**
 * Describes the status of a study.
 */
@ImplementAsDataClass
sealed class StudyStatus
{
    /**
     * Unique ID of the study on the client manager.
     */
    abstract val id: UUID


    /**
     * The study deployment process hasn't been started yet.
     */
    data class DeploymentNotStarted( override val id: UUID ) : StudyStatus()

    /**
     * Once a deployment for this study has started, a [deploymentStatus] is available,
     * regardless of whether the deployment is now [Running] or [Stopped].
     */
    interface DeploymentStatusAvailable
    {
        val deploymentStatus: StudyDeploymentStatus
    }

    /**
     * The study deployment process is ongoing, but not yet completed.
     * The state of the deployment can be tracked using [deploymentStatus].
     */
    sealed class Deploying : StudyStatus(), DeploymentStatusAvailable
    {
        companion object
        {
            /**
             * Initialize a [Deploying] state for a study deployment that is currently [deployingDevices]
             * for a client which may or may not yet have received its [deploymentInformation].
             */
            fun fromStudyDeploymentStatus(
                id: UUID,
                deviceRoleName: String,
                deployingDevices: StudyDeploymentStatus.DeployingDevices,
                deploymentInformation: PrimaryDeviceDeployment?
            ): Deploying
            {
                return when ( val deviceStatus = deployingDevices.getDeviceStatus( deviceRoleName ) )
                {
                    is DeviceDeploymentStatus.Unregistered -> error( "Client device should already be registered." )
                    is DeviceDeploymentStatus.NotDeployed ->
                        if ( deploymentInformation == null || deviceStatus is DeviceDeploymentStatus.NeedsRedeployment )
                        {
                            if ( deviceStatus.canObtainDeviceDeployment ) AwaitingDeviceDeployment( id, deployingDevices )
                            else AwaitingOtherDeviceRegistrations( id, deployingDevices )
                        }
                        else
                        {
                            RegisteringDevices( id, deployingDevices, deploymentInformation )
                        }
                    is DeviceDeploymentStatus.Deployed ->
                        AwaitingOtherDeviceDeployments( id, deployingDevices, checkNotNull( deploymentInformation ) )
                }
            }
        }
    }

    /**
     * Deployment information for this primary device cannot be retrieved yet since
     * other primary devices in the study deployment need to be registered first.
     */
    data class AwaitingOtherDeviceRegistrations(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus
    ) : Deploying()

    /**
     * The study deployment is ready to deliver the deployment information to this primary device.
     */
    data class AwaitingDeviceDeployment(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus
    ) : Deploying()

    /**
     * Deployment information has been received.
     */
    interface DeviceDeploymentReceived : DeploymentStatusAvailable
    {
        // TODO: This should be consumed within this domain model and not be public.
        //  Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
        val deploymentInformation: PrimaryDeviceDeployment

        /**
         * The current [DeviceRegistrationStatus] for the client device and each of its connected devices.
         */
        val devicesRegistrationStatus: Map<AnyDeviceConfiguration, DeviceRegistrationStatus>
    }

    /**
     * The device deployment of this primary device can complete
     * once all [remainingDevicesToRegister] have been registered.
     */
    data class RegisteringDevices(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus,
        override val deploymentInformation: PrimaryDeviceDeployment
    ) : Deploying(), DeviceDeploymentReceived
    {
        override val devicesRegistrationStatus: Map<AnyDeviceConfiguration, DeviceRegistrationStatus> =
            getDevicesRegistrationStatus( deploymentInformation )

        val remainingDevicesToRegister: Set<AnyDeviceConfiguration> = deploymentInformation
            .getRuntimeDeviceInfo()
            .filter { it.isConnectedDevice && it.registration == null }
            .map { it.configuration }
            .toSet()
    }

    /**
     * Device deployment for this primary device has completed,
     * but awaiting deployment of other devices in this study deployment.
     */
    data class AwaitingOtherDeviceDeployments(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus,
        override val deploymentInformation: PrimaryDeviceDeployment,
    ) : Deploying(), DeviceDeploymentReceived
    {
        override val devicesRegistrationStatus: Map<AnyDeviceConfiguration, DeviceRegistrationStatus> =
            getDevicesRegistrationStatus( deploymentInformation )
    }

    /**
     * Study deployment has completed and the study is now running.
     */
    data class Running(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus.Running,
        override val deploymentInformation: PrimaryDeviceDeployment
    ) : StudyStatus(), DeviceDeploymentReceived, DeploymentStatusAvailable
    {
        override val devicesRegistrationStatus: Map<AnyDeviceConfiguration, DeviceRegistrationStatus> =
            getDevicesRegistrationStatus( deploymentInformation )
    }

    /**
     * Study status when deployment has been stopped, either by this client or researcher.
     */
    data class Stopped internal constructor(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus,
        val deploymentInformation: PrimaryDeviceDeployment?
    ) : StudyStatus(), DeploymentStatusAvailable
}


// TODO: By remote device unregister/register calls it can happen that registrations in `PrimaryDeviceDeployment`
//  differ from those in `StudyDeploymentStatus`. This needs to be taken into account.
private fun getDevicesRegistrationStatus( deployment: PrimaryDeviceDeployment ) = deployment
    .getRuntimeDeviceInfo()
    .map {
        val registration = it.registration
        if ( registration == null ) DeviceRegistrationStatus.Unregistered( it.configuration )
        else DeviceRegistrationStatus.Registered( it.configuration, registration )
    }.associateBy { it.device }
