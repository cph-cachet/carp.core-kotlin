package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.application.ParticipationService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [ParticipationService] which can be executed on demand.
 */
@Serializable
sealed class ParticipationServiceRequest
{
    @Serializable
    data class AddParticipation( val studyDeploymentId: UUID, val identity: AccountIdentity, val invitation: StudyInvitation ) :
        ParticipationServiceRequest(),
        ServiceInvoker<ParticipationService, Participation> by createServiceInvoker( ParticipationService::addParticipation, studyDeploymentId, identity, invitation )

    @Serializable
    data class GetParticipationsForStudyDeployment( val studyId: UUID ) :
        ParticipationServiceRequest(),
        ServiceInvoker<ParticipationService, List<Participation>> by createServiceInvoker( ParticipationService::getParticipationsForStudyDeployment, studyId )
}
