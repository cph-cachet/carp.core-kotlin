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

private typealias Service = DeploymentService


class DeploymentServiceMock(
    private val createStudyDeploymentResult: StudyDeploymentStatus = emptyStatus,
    private val getStudyDeploymentStatusResult: StudyDeploymentStatus = emptyStatus,
    private val getStudyDeploymentStatusesResult: List<StudyDeploymentStatus> = emptyList(),
    private val registerDeviceResult: StudyDeploymentStatus = emptyStatus,
    private val unregisterDeviceResult: StudyDeploymentStatus = emptyStatus,
    private val getDeviceDeploymentForResult: MasterDeviceDeployment = emptyMasterDeviceDeployment,
    private val deploymentSuccessfulResult: StudyDeploymentStatus = emptyStatus,
    private val stopResult: StudyDeploymentStatus = emptyStatus,
    private val getParticipationInvitationResult: Set<ParticipationInvitation> = emptySet()
) : Mock<Service>(), Service
{
    companion object
    {
        private val emptyStatus: StudyDeploymentStatus = StudyDeploymentStatus.DeployingDevices(
            UUID( "00000000-0000-0000-0000-000000000000"),
            listOf() )
        private val emptyMasterDeviceDeployment: MasterDeviceDeployment = MasterDeviceDeployment(
            DefaultDeviceRegistration( "Test" ),
            setOf(), mapOf(), setOf(), mapOf(), setOf() )
    }


    override suspend fun createStudyDeployment( protocol: StudyProtocolSnapshot ) =
        createStudyDeploymentResult
        .also { trackSuspendCall( Service::createStudyDeployment, protocol ) }

    override suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ) =
        getStudyDeploymentStatusResult
        .also { trackSuspendCall( Service::getStudyDeploymentStatus, studyDeploymentId ) }

    override suspend fun getStudyDeploymentStatuses( studyDeploymentIds: Set<UUID> ) =
        getStudyDeploymentStatusesResult
        .also { trackSuspendCall( Service::getStudyDeploymentStatuses, studyDeploymentIds ) }

    override suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ) =
        registerDeviceResult
        .also { trackSuspendCall( Service::registerDevice, studyDeploymentId, deviceRoleName, registration ) }

    override suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ) =
        unregisterDeviceResult
        .also { trackSuspendCall( Service::unregisterDevice, studyDeploymentId, deviceRoleName ) }

    override suspend fun getDeviceDeploymentFor( studyDeploymentId: UUID, masterDeviceRoleName: String ) =
        getDeviceDeploymentForResult
        .also { trackSuspendCall( Service::getDeviceDeploymentFor, studyDeploymentId, masterDeviceRoleName ) }

    override suspend fun deploymentSuccessful( studyDeploymentId: UUID, masterDeviceRoleName: String, deploymentChecksum: Int ) =
        deploymentSuccessfulResult
        .also { trackSuspendCall( Service::deploymentSuccessful, studyDeploymentId, masterDeviceRoleName, deploymentChecksum ) }

    override suspend fun stop( studyDeploymentId: UUID ) =
        stopResult
        .also { trackSuspendCall( Service::stop, studyDeploymentId ) }

    override suspend fun addParticipation( studyDeploymentId: UUID, deviceRoleNames: Set<String>, identity: AccountIdentity, invitation: StudyInvitation ) =
        Participation( studyDeploymentId )
        .also { trackSuspendCall( Service::addParticipation, studyDeploymentId, deviceRoleNames, identity, invitation ) }

    override suspend fun getParticipationInvitations( accountId: UUID ) =
        getParticipationInvitationResult
        .also { trackSuspendCall( Service::getParticipationInvitations, accountId ) }
}
