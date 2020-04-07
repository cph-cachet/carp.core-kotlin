package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationSerializer
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [DeploymentService] which can be executed on demand.
 */
@Serializable
sealed class DeploymentServiceRequest
{
    @Serializable
    data class CreateStudyDeployment( val protocol: StudyProtocolSnapshot ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::createStudyDeployment, protocol )

    @Serializable
    data class GetStudyDeploymentStatus( val studyDeploymentId: UUID ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::getStudyDeploymentStatus, studyDeploymentId )

    @Serializable
    data class RegisterDevice(
        val studyDeploymentId: UUID,
        val deviceRoleName: String,
        @Serializable( DeviceRegistrationSerializer::class )
        val registration: DeviceRegistration
    ) : DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::registerDevice, studyDeploymentId, deviceRoleName, registration )

    @Serializable
    data class UnregisterDevice( val studyDeploymentId: UUID, val deviceRoleName: String ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::unregisterDevice, studyDeploymentId, deviceRoleName )

    @Serializable
    data class GetDeviceDeploymentFor( val studyDeploymentId: UUID, val masterDeviceRoleName: String ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, MasterDeviceDeployment> by createServiceInvoker( DeploymentService::getDeviceDeploymentFor, studyDeploymentId, masterDeviceRoleName )

    @Serializable
    data class DeploymentSuccessful( val studyDeploymentId: UUID, val masterDeviceRoleName: String ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::deploymentSuccessful, studyDeploymentId, masterDeviceRoleName )

    @Serializable
    data class AddParticipation( val studyDeploymentId: UUID, val deviceRoleNames: Set<String>, val identity: AccountIdentity, val invitation: StudyInvitation ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, Participation> by createServiceInvoker( DeploymentService::addParticipation, studyDeploymentId, deviceRoleNames, identity, invitation )

    @Serializable
    data class GetParticipationInvitations( val accountId: UUID ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, Set<ParticipationInvitation>> by createServiceInvoker( DeploymentService::getParticipationInvitations, accountId )
}
