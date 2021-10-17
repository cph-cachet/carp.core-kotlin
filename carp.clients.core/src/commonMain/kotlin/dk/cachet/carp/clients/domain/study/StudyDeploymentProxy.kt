package dk.cachet.carp.clients.domain.study

import dk.cachet.carp.clients.domain.Study
import dk.cachet.carp.clients.domain.StudyStatus
import dk.cachet.carp.clients.domain.data.DataListener
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.StudyDeploymentStatus


/**
 * Perform deployment actions for a [Study] on a client device.
 */
class StudyDeploymentProxy(
    private val deploymentService: DeploymentService,
    private val dataListener: DataListener
)
{
    /**
     * Tries to deploy the [study] if it's ready to be deployed
     * by registering the client device using [deviceRegistration] and verifying the study is supported on this device.
     * In case already deployed, nothing happens.
     *
     * TODO: Handle `NeedsRedeployment`, invalidating the retrieved deployment information.
     * @throws IllegalArgumentException when:
     *  - no study deployment to deploy this client device for found
     *  - the client device is not expected in the deployment or is already registered and a different [deviceRegistration] is specified
     *  - [deviceRegistration] is invalid for the client device or
     *    has a device ID which is already in use by the registration of a different device
     * @throws UnsupportedOperationException when:
     *  - not all necessary plugins to execute the study are available
     *  - data requested in the deployment cannot be collected on this client device
     */
    suspend fun tryDeployment( study: Study, deviceRegistration: DeviceRegistration )
    {
        val (studyDeploymentId: UUID, deviceRoleName: String) = study.id

        // Register the client device in the study deployment.
        val studyStatus: StudyDeploymentStatus =
            deploymentService.registerDevice( studyDeploymentId, deviceRoleName, deviceRegistration )
        val deviceStatus = studyStatus.getDeviceStatus( deviceRoleName )

        // Early out in case state indicates the device is already deployed or deployment cannot yet be obtained.
        if ( deviceStatus !is DeviceDeploymentStatus.NotDeployed ) return
        if ( !deviceStatus.canObtainDeviceDeployment ) return

        // Get deployment information.
        // TODO: Handle race condition in case other devices were unregistered in between.
        val device = deviceStatus.device
        val deployment = deploymentService.getDeviceDeploymentFor( studyDeploymentId, device.roleName )
        check( deployment.deviceDescriptor == device )
        val remainingDevicesToRegister = studyStatus.devicesStatus
            .map { it.device }
            .filter { it.roleName in deviceStatus.remainingDevicesToRegisterBeforeDeployment }
            .toSet()
        study.deploymentReceived( deployment, remainingDevicesToRegister )

        // Early out in case devices need to be registered before being able to complete deployment.
        if ( remainingDevicesToRegister.isNotEmpty() ) return

        // Validate deployment and notify deployment service in case successful.
        study.completeDeployment( dataListener )
        try
        {
            deploymentService.deploymentSuccessful( studyDeploymentId, device.roleName, deployment.lastUpdatedOn )
        }
        // Handle race conditions with competing clients modifying device registrations, invalidating this deployment.
        catch ( ignore: IllegalArgumentException ) { } // TODO: When deployment is out of date, maybe also use `IllegalStateException` for easier handling here.
        catch ( ignore: IllegalStateException ) { }
    }

    /**
     * Stop the study deployment which this [study] runtime is part of.
     */
    suspend fun stop( study: Study )
    {
        // Early out in case study has already been stopped.
        val status = study.getStatus()
        if ( status is StudyStatus.Stopped ) return

        // Stop study deployment.
        // TODO: Right now this requires the client to be online in case `deploymentService` is an online service.
        //       Once we have domain events in place this should be modeled as a request to stop deployment which is cached when offline.
        val deploymentStatus = deploymentService.stop( study.studyDeploymentId )
        check( deploymentStatus is StudyDeploymentStatus.Stopped )

        study.stop()
    }
}
