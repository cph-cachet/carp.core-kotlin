package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.application.UserService
import dk.cachet.carp.deployment.domain.users.Participation
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [UserService] which can be executed on demand.
 */
@Serializable
sealed class UserServiceRequest
{
    @Serializable
    data class AddParticipation( val studyDeploymentId: UUID, val identity: AccountIdentity ) :
        UserServiceRequest(),
        ServiceInvoker<UserService, Participation> by createServiceInvoker( UserService::addParticipation, studyDeploymentId, identity )

    @Serializable
    data class GetParticipationsForStudyDeployment( val studyId: UUID ) :
        UserServiceRequest(),
        ServiceInvoker<UserService, List<Participation>> by createServiceInvoker( UserService::getParticipationsForStudyDeployment, studyId )
}
