package dk.cachet.carp.clients.domain

import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.deployments.application.MasterDeviceDeployment


/**
 * Describes the status of a [StudyRuntime].
 */
@ImplementAsDataClass
sealed class StudyRuntimeStatus
{
    /**
     * Unique ID of the study runtime on the [ClientManager].
     */
    abstract val id: StudyRuntimeId


    /**
     * Deployment information has been received.
     */
    interface DeploymentReceived
    {
        val id: StudyRuntimeId

        /**
         * Contains all the information on the study to run.
         *
         * TODO: This should be consumed within this domain model and not be public.
         *       Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
         */
        val deploymentInformation: MasterDeviceDeployment

        /**
         * The [DeviceRegistrationStatus] for the master device and each of the devices this device needs to connect to.
         */
        val devicesRegistrationStatus: Map<AnyDeviceDescriptor, DeviceRegistrationStatus>
    }

    /**
     * Deployment cannot succeed yet because other master devices have not been registered yet.
     */
    data class NotReadyForDeployment internal constructor( override val id: StudyRuntimeId ) : StudyRuntimeStatus()

    /**
     * Deployment can complete after [remainingDevicesToRegister] have been registered.
     */
    data class RegisteringDevices internal constructor(
        override val id: StudyRuntimeId,
        override val deploymentInformation: MasterDeviceDeployment,
        val remainingDevicesToRegister: Set<AnyDeviceDescriptor>
    ) : StudyRuntimeStatus(), DeploymentReceived
    {
        override val devicesRegistrationStatus = getDevicesRegistrationStatus( deploymentInformation )
    }

    /**
     * Study runtime status when deployment has been successfully completed:
     * the [MasterDeviceDeployment] has been retrieved and all necessary plugins to execute the study have been loaded.
     */
    data class Deployed internal constructor(
        override val id: StudyRuntimeId,
        override val deploymentInformation: MasterDeviceDeployment
    ) : StudyRuntimeStatus(), DeploymentReceived
    {
        override val devicesRegistrationStatus = getDevicesRegistrationStatus( deploymentInformation )
    }

    /**
     * Study runtime status when deployment has been stopped, either by this client or researcher.
     */
    data class Stopped internal constructor(
        override val id: StudyRuntimeId,
        override val deploymentInformation: MasterDeviceDeployment
    ) : StudyRuntimeStatus(), DeploymentReceived
    {
        override val devicesRegistrationStatus = getDevicesRegistrationStatus( deploymentInformation )
    }
}


private fun getDevicesRegistrationStatus( deployment: MasterDeviceDeployment ) = deployment
    .getRuntimeDeviceInfo()
    .map {
        val registration = it.registration
        if (registration == null) DeviceRegistrationStatus.Unregistered(it.descriptor)
        else DeviceRegistrationStatus.Registered(it.descriptor, registration)
    }.associateBy { it.device }
