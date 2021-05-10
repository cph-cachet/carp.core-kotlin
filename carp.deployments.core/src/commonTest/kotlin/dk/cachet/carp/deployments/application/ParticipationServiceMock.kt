package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.DeanonymizedParticipation
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.test.Mock

// TODO: Due to a bug, `Service` cannot be used here, although that would be preferred.
//       Change this once this is fixed: https://youtrack.jetbrains.com/issue/KT-24700
// private typealias Service = ParticipationService


class ParticipationServiceMock(
    private val deanonymizeParticipationsResult: Set<DeanonymizedParticipation> = emptySet(),
    private val getActiveParticipationInvitationResult: Set<ActiveParticipationInvitation> = emptySet(),
    private val getParticipantDataResult: ParticipantData = ParticipantData( UUID.randomUUID(), emptyMap() ),
    private val getParticipantDataListResult: List<ParticipantData> = emptyList(),
    private val setParticipantDataResult: ParticipantData = ParticipantData( UUID.randomUUID(), emptyMap() )
) : Mock<ParticipationService>(), ParticipationService
{
    override suspend fun addParticipation( studyDeploymentId: UUID, externalParticipantId: UUID, deviceRoleNames: Set<String>, identity: AccountIdentity, invitation: StudyInvitation ) =
        Participation( studyDeploymentId )
        .also { trackSuspendCall( ParticipationService::addParticipation, studyDeploymentId, externalParticipantId, deviceRoleNames, identity, invitation ) }

    override suspend fun deanonymizeParticipations( studyDeploymentId: UUID, externalParticipantIds: Set<UUID> ) =
        deanonymizeParticipationsResult
        .also { trackSuspendCall( ParticipationService::deanonymizeParticipations, studyDeploymentId, externalParticipantIds ) }

    override suspend fun getActiveParticipationInvitations( accountId: UUID ) =
        getActiveParticipationInvitationResult
        .also { trackSuspendCall( ParticipationService::getActiveParticipationInvitations, accountId ) }

    override suspend fun getParticipantData( studyDeploymentId: UUID ): ParticipantData =
        getParticipantDataResult
        .also { trackSuspendCall( ParticipationService::getParticipantData, studyDeploymentId ) }

    override suspend fun getParticipantDataList( studyDeploymentIds: Set<UUID> ): List<ParticipantData> =
        getParticipantDataListResult
        .also { trackSuspendCall( ParticipationService::getParticipantDataList, studyDeploymentIds ) }

    override suspend fun setParticipantData( studyDeploymentId: UUID, inputDataType: InputDataType, data: Data? ) =
        setParticipantDataResult
        .also { trackSuspendCall( ParticipationService::setParticipantData, studyDeploymentId, inputDataType, data ) }
}
