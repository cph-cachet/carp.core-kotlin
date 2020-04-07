package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.test.Mock


class DeploymentServiceMock(
    private val createStudyDeploymentResult: StudyDeploymentStatus = emptyStatus,
    private val getStudyDeploymentStatusResult: StudyDeploymentStatus = emptyStatus,
    private val registerDeviceResult: StudyDeploymentStatus = emptyStatus,
    private val unregisterDeviceResult: StudyDeploymentStatus = emptyStatus,
    private val getDeviceDeploymentForResult: MasterDeviceDeployment = emptyMasterDeviceDeployment,
    private val deploymentSuccessfulResult: StudyDeploymentStatus = emptyStatus,
    private val getParticipationInvitationResult: Set<ParticipationInvitation> = setOf()
) : Mock<DeploymentService>(), DeploymentService
{
    companion object
    {
        private val emptyStatus: StudyDeploymentStatus = StudyDeploymentStatus(
            UUID( "00000000-0000-0000-0000-000000000000"),
            listOf() )
        private val emptyMasterDeviceDeployment: MasterDeviceDeployment = MasterDeviceDeployment(
            DefaultDeviceRegistration( "Test" ),
            setOf(), mapOf(), setOf(), mapOf(), setOf() )
    }


    override suspend fun createStudyDeployment( protocol: StudyProtocolSnapshot ): StudyDeploymentStatus
    {
        trackSuspendCall( DeploymentService::createStudyDeployment, protocol )
        return createStudyDeploymentResult
    }

    override suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ): StudyDeploymentStatus
    {
        trackSuspendCall( DeploymentService::getStudyDeploymentStatus, studyDeploymentId )
        return getStudyDeploymentStatusResult
    }

    override suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ): StudyDeploymentStatus
    {
        trackSuspendCall( DeploymentService::registerDevice, studyDeploymentId, deviceRoleName, registration )
        return registerDeviceResult
    }

    override suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ): StudyDeploymentStatus
    {
        trackSuspendCall( DeploymentService::unregisterDevice, studyDeploymentId, deviceRoleName )
        return unregisterDeviceResult
    }

    override suspend fun getDeviceDeploymentFor( studyDeploymentId: UUID, masterDeviceRoleName: String ): MasterDeviceDeployment
    {
        trackSuspendCall( DeploymentService::getDeviceDeploymentFor, studyDeploymentId, masterDeviceRoleName )
        return getDeviceDeploymentForResult
    }

    override suspend fun deploymentSuccessful( studyDeploymentId: UUID, masterDeviceRoleName: String, deploymentChecksum: Int ): StudyDeploymentStatus
    {
        trackSuspendCall( DeploymentService::deploymentSuccessful, studyDeploymentId, masterDeviceRoleName, deploymentChecksum )
        return deploymentSuccessfulResult
    }

    override suspend fun addParticipation( studyDeploymentId: UUID, deviceRoleNames: Set<String>, identity: AccountIdentity, invitation: StudyInvitation ): Participation
    {
        trackSuspendCall( DeploymentService::addParticipation, studyDeploymentId, deviceRoleNames, identity, invitation )
        return Participation( studyDeploymentId )
    }

    override suspend fun getParticipationInvitations( accountId: UUID ): Set<ParticipationInvitation>
    {
        trackSuspendCall( DeploymentService::getParticipationInvitations, accountId )
        return getParticipationInvitationResult
    }
}
