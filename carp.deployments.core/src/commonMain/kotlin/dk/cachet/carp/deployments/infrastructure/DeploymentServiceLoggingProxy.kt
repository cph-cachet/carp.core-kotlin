package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.datetime.Instant


/**
 * A proxy for a deployment [service] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests] and published events in [loggedEvents].
 */
class DeploymentServiceLoggingProxy(
    service: DeploymentService,
    eventBus: EventBus,
    log: (LoggedRequest<DeploymentService, DeploymentService.Event>) -> Unit = { }
) :
    ApplicationServiceLoggingProxy<DeploymentService, DeploymentService.Event>(
        service,
        EventBusLog(
            eventBus,
            EventBusLog.Subscription( DeploymentService::class, DeploymentService.Event::class )
        ),
        log
    ),
    DeploymentService
{
    override suspend fun createStudyDeployment(
        id: UUID,
        protocol: StudyProtocolSnapshot,
        invitations: List<ParticipantInvitation>,
        connectedDevicePreregistrations: Map<String, DeviceRegistration>
    ): StudyDeploymentStatus = log(
        DeploymentServiceRequest.CreateStudyDeployment( id, protocol, invitations, connectedDevicePreregistrations )
    )

    override suspend fun removeStudyDeployments( studyDeploymentIds: Set<UUID> ): Set<UUID> =
        log( DeploymentServiceRequest.RemoveStudyDeployments( studyDeploymentIds ) )

    override suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ): StudyDeploymentStatus =
        log( DeploymentServiceRequest.GetStudyDeploymentStatus( studyDeploymentId ) )

    override suspend fun getStudyDeploymentStatusList( studyDeploymentIds: Set<UUID> ): List<StudyDeploymentStatus> =
        log( DeploymentServiceRequest.GetStudyDeploymentStatusList( studyDeploymentIds ) )

    override suspend fun registerDevice(
        studyDeploymentId: UUID,
        deviceRoleName: String,
        registration: DeviceRegistration
    ): StudyDeploymentStatus =
        log( DeploymentServiceRequest.RegisterDevice( studyDeploymentId, deviceRoleName, registration) )

    override suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ): StudyDeploymentStatus =
        log( DeploymentServiceRequest.UnregisterDevice( studyDeploymentId, deviceRoleName ) )

    override suspend fun getDeviceDeploymentFor(
        studyDeploymentId: UUID,
        masterDeviceRoleName: String
    ): MasterDeviceDeployment =
        log( DeploymentServiceRequest.GetDeviceDeploymentFor( studyDeploymentId, masterDeviceRoleName ) )

    override suspend fun deviceDeployed(
        studyDeploymentId: UUID,
        masterDeviceRoleName: String,
        deviceDeploymentLastUpdatedOn: Instant
    ): StudyDeploymentStatus = log(
        DeploymentServiceRequest.DeviceDeployed(
            studyDeploymentId,
            masterDeviceRoleName,
            deviceDeploymentLastUpdatedOn
        )
    )

    override suspend fun stop( studyDeploymentId: UUID ): StudyDeploymentStatus =
        log( DeploymentServiceRequest.Stop( studyDeploymentId ) )
}
