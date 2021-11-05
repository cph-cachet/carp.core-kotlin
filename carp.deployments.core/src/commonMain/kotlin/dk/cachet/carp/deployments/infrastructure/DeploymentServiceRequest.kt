package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.services.createServiceInvoker
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

private typealias Service = DeploymentService
private typealias Invoker<T> = ServiceInvoker<DeploymentService, T>


/**
 * Serializable application service requests to [DeploymentService] which can be executed on demand.
 */
@Serializable
sealed class DeploymentServiceRequest
{
    @Serializable
    data class CreateStudyDeployment(
        val id: UUID,
        val protocol: StudyProtocolSnapshot,
        val invitations: List<ParticipantInvitation>,
        val connectedDevicePreregistrations: Map<String, DeviceRegistration> = emptyMap()
    ) : DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::createStudyDeployment, id, protocol, invitations, connectedDevicePreregistrations )

    @Serializable
    data class RemoveStudyDeployments( val studyDeploymentIds: Set<UUID> ) :
        DeploymentServiceRequest(),
        Invoker<Set<UUID>> by createServiceInvoker( Service::removeStudyDeployments, studyDeploymentIds )

    @Serializable
    data class GetStudyDeploymentStatus( val studyDeploymentId: UUID ) :
        DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::getStudyDeploymentStatus, studyDeploymentId )

    @Serializable
    data class GetStudyDeploymentStatusList( val studyDeploymentIds: Set<UUID> ) :
        DeploymentServiceRequest(),
        Invoker<List<StudyDeploymentStatus>> by createServiceInvoker( Service::getStudyDeploymentStatusList, studyDeploymentIds )

    @Serializable
    data class RegisterDevice(
        val studyDeploymentId: UUID,
        val deviceRoleName: String,
        val registration: DeviceRegistration
    ) : DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::registerDevice, studyDeploymentId, deviceRoleName, registration )

    @Serializable
    data class UnregisterDevice( val studyDeploymentId: UUID, val deviceRoleName: String ) :
        DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::unregisterDevice, studyDeploymentId, deviceRoleName )

    @Serializable
    data class GetDeviceDeploymentFor( val studyDeploymentId: UUID, val masterDeviceRoleName: String ) :
        DeploymentServiceRequest(),
        Invoker<MasterDeviceDeployment> by createServiceInvoker( Service::getDeviceDeploymentFor, studyDeploymentId, masterDeviceRoleName )

    @Serializable
    data class DeviceDeployed(
        val studyDeploymentId: UUID,
        val masterDeviceRoleName: String,
        val deviceDeploymentLastUpdatedOn: Instant
    ) : DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::deviceDeployed, studyDeploymentId, masterDeviceRoleName, deviceDeploymentLastUpdatedOn )

    @Serializable
    data class Stop( val studyDeploymentId: UUID ) :
        DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::stop, studyDeploymentId )
}
