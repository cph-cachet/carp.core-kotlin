package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.studies.application.ParticipantService
import dk.cachet.carp.studies.domain.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import kotlinx.serialization.Serializable

// TODO: Due to a bug, `Service` and `Invoker` cannot be used here, although that would be preferred.
//       Change this once this is fixed: https://youtrack.jetbrains.com/issue/KT-24700
private typealias ParticipantServiceInvoker<T> = ServiceInvoker<ParticipantService, T>
// private typealias Service = ParticipantService
// private typealias Invoker<T> = ServiceInvoker<ParticipantService, T>


/**
 * Serializable application service requests to [ParticipantService] which can be executed on demand.
 */
@Serializable
sealed class ParticipantServiceRequest
{
    @Serializable
    data class AddParticipant( val studyId: UUID, val email: EmailAddress ) :
        ParticipantServiceRequest(),
        ParticipantServiceInvoker<Participant> by createServiceInvoker( ParticipantService::addParticipant, studyId, email )

    @Serializable
    data class GetParticipant( val studyId: UUID, val participantId: UUID ) :
        ParticipantServiceRequest(),
        ParticipantServiceInvoker<Participant> by createServiceInvoker( ParticipantService::getParticipant, studyId, participantId )

    @Serializable
    data class GetParticipants( val studyId: UUID ) :
        ParticipantServiceRequest(),
        ParticipantServiceInvoker<List<Participant>> by createServiceInvoker( ParticipantService::getParticipants, studyId )

    @Serializable
    data class DeployParticipantGroup( val studyId: UUID, val group: Set<AssignParticipantDevices> ) :
        ParticipantServiceRequest(),
        ParticipantServiceInvoker<ParticipantGroupStatus> by createServiceInvoker( ParticipantService::deployParticipantGroup, studyId, group )

    @Serializable
    data class GetParticipantGroupStatusList( val studyId: UUID ) :
        ParticipantServiceRequest(),
        ParticipantServiceInvoker<List<ParticipantGroupStatus>> by createServiceInvoker( ParticipantService::getParticipantGroupStatusList, studyId )

    @Serializable
    data class StopParticipantGroup( val studyId: UUID, val groupId: UUID ) :
        ParticipantServiceRequest(),
        ParticipantServiceInvoker<ParticipantGroupStatus> by createServiceInvoker( ParticipantService::stopParticipantGroup, studyId, groupId )
}
