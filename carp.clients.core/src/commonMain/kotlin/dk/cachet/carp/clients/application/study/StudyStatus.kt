package dk.cachet.carp.clients.application.study

import dk.cachet.carp.clients.domain.DeviceRegistrationStatus
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
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
                deploymentInformation: MasterDeviceDeployment?
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
     * Deployment information for this master device cannot be retrieved yet since
     * other master devices in the study deployment need to be registered first.
     */
    data class AwaitingOtherDeviceRegistrations(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus
    ) : Deploying()

    /**
     * The study deployment is ready to deliver the deployment information to this master device.
     */
    data class AwaitingDeviceDeployment(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus
    ) : Deploying()

    /**
     * Deployment information has been received.
     */
    interface DeviceDeploymentReceived
    {
        // TODO: This should be consumed within this domain model and not be public.
        //  Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
        val deploymentInformation: MasterDeviceDeployment

        /**
         * The current [DeviceRegistrationStatus] for the client device and each of its connected devices.
         */
        val devicesRegistrationStatus: Map<AnyDeviceDescriptor, DeviceRegistrationStatus>
    }

    /**
     * The device deployment of this master device can complete
     * once all [remainingDevicesToRegister] have been registered.
     */
    data class RegisteringDevices(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus,
        override val deploymentInformation: MasterDeviceDeployment
    ) : Deploying(), DeviceDeploymentReceived
    {
        override val devicesRegistrationStatus: Map<AnyDeviceDescriptor, DeviceRegistrationStatus> =
            getDevicesRegistrationStatus( deploymentInformation )

        val remainingDevicesToRegister: Set<AnyDeviceDescriptor> = deploymentInformation
            .getRuntimeDeviceInfo()
            .filter { it.isConnectedDevice && it.registration == null }
            .map { it.descriptor }
            .toSet()
    }

    /**
     * Device deployment for this master device has completed,
     * but awaiting deployment of other devices in this study deployment.
     */
    data class AwaitingOtherDeviceDeployments(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus,
        override val deploymentInformation: MasterDeviceDeployment,
    ) : Deploying(), DeviceDeploymentReceived
    {
        override val devicesRegistrationStatus: Map<AnyDeviceDescriptor, DeviceRegistrationStatus> =
            getDevicesRegistrationStatus( deploymentInformation )
    }

    /**
     * Study deployment has completed and the study is now running.
     */
    data class Running(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus,
        override val deploymentInformation: MasterDeviceDeployment
    ) : StudyStatus(), DeviceDeploymentReceived, DeploymentStatusAvailable
    {
        override val devicesRegistrationStatus: Map<AnyDeviceDescriptor, DeviceRegistrationStatus> =
            getDevicesRegistrationStatus( deploymentInformation )
    }

    /**
     * Study status when deployment has been stopped, either by this client or researcher.
     */
    data class Stopped internal constructor(
        override val id: UUID,
        override val deploymentStatus: StudyDeploymentStatus,
        val deploymentInformation: MasterDeviceDeployment?
    ) : StudyStatus(), DeploymentStatusAvailable
}


// TODO: By remote device unregister/register calls it can happen that registrations in `MasterDeviceDeployment`
//  differ from those in `StudyDeploymentStatus`. This needs to be taken into account.
private fun getDevicesRegistrationStatus( deployment: MasterDeviceDeployment ) = deployment
    .getRuntimeDeviceInfo()
    .map {
        val registration = it.registration
        if ( registration == null ) DeviceRegistrationStatus.Unregistered( it.descriptor )
        else DeviceRegistrationStatus.Registered( it.descriptor, registration )
    }.associateBy { it.device }
