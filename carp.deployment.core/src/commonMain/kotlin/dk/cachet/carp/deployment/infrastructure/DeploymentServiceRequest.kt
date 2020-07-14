package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationSerializer
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
    data class CreateStudyDeployment( val protocol: StudyProtocolSnapshot ) :
        DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::createStudyDeployment, protocol )

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
        @Serializable( DeviceRegistrationSerializer::class )
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
    data class DeploymentSuccessful( val studyDeploymentId: UUID, val masterDeviceRoleName: String, val deviceDeploymentLastUpdateDate: DateTime ) :
        DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::deploymentSuccessful, studyDeploymentId, masterDeviceRoleName, deviceDeploymentLastUpdateDate )

    @Serializable
    data class Stop( val studyDeploymentId: UUID ) :
        DeploymentServiceRequest(),
        Invoker<StudyDeploymentStatus> by createServiceInvoker( Service::stop, studyDeploymentId )

    @Serializable
    data class AddParticipation( val studyDeploymentId: UUID, val deviceRoleNames: Set<String>, val identity: AccountIdentity, val invitation: StudyInvitation ) :
        DeploymentServiceRequest(),
        Invoker<Participation> by createServiceInvoker( Service::addParticipation, studyDeploymentId, deviceRoleNames, identity, invitation )

    @Serializable
    data class GetActiveParticipationInvitations( val accountId: UUID ) :
        DeploymentServiceRequest(),
        Invoker<Set<ActiveParticipationInvitation>> by createServiceInvoker( Service::getActiveParticipationInvitations, accountId )
}
