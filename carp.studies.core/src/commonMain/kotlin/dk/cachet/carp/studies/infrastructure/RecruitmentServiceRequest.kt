package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.services.createServiceInvoker
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlinx.serialization.Serializable

// TODO: Due to a bug, `Service` and `Invoker` cannot be used here, although that would be preferred.
//       Change this once this is fixed: https://youtrack.jetbrains.com/issue/KT-24700
private typealias RecruitmentServiceInvoker<T> = ServiceInvoker<RecruitmentService, T>
// private typealias Service = ParticipantService
// private typealias Invoker<T> = ServiceInvoker<ParticipantService, T>


/**
 * Serializable application service requests to [RecruitmentService] which can be executed on demand.
 */
@Serializable
sealed class RecruitmentServiceRequest
{
    @Serializable
    data class AddParticipant( val studyId: UUID, val email: EmailAddress ) :
        RecruitmentServiceRequest(),
        RecruitmentServiceInvoker<Participant> by createServiceInvoker( RecruitmentService::addParticipant, studyId, email )

    @Serializable
    data class GetParticipant( val studyId: UUID, val participantId: UUID ) :
        RecruitmentServiceRequest(),
        RecruitmentServiceInvoker<Participant> by createServiceInvoker( RecruitmentService::getParticipant, studyId, participantId )

    @Serializable
    data class GetParticipants( val studyId: UUID ) :
        RecruitmentServiceRequest(),
        RecruitmentServiceInvoker<List<Participant>> by createServiceInvoker( RecruitmentService::getParticipants, studyId )

    @Serializable
    data class DeployParticipantGroup( val studyId: UUID, val group: Set<AssignParticipantDevices> ) :
        RecruitmentServiceRequest(),
        RecruitmentServiceInvoker<ParticipantGroupStatus> by createServiceInvoker( RecruitmentService::deployParticipantGroup, studyId, group )

    @Serializable
    data class GetParticipantGroupStatusList( val studyId: UUID ) :
        RecruitmentServiceRequest(),
        RecruitmentServiceInvoker<List<ParticipantGroupStatus>> by createServiceInvoker( RecruitmentService::getParticipantGroupStatusList, studyId )

    @Serializable
    data class StopParticipantGroup( val studyId: UUID, val groupId: UUID ) :
        RecruitmentServiceRequest(),
        RecruitmentServiceInvoker<ParticipantGroupStatus> by createServiceInvoker( RecruitmentService::stopParticipantGroup, studyId, groupId )

    @Serializable
    data class SetParticipantGroupData( val studyId: UUID, val groupId: UUID, val inputDataType: InputDataType, val data: Data? ) :
        RecruitmentServiceRequest(),
        RecruitmentServiceInvoker<ParticipantGroupStatus> by createServiceInvoker( RecruitmentService::setParticipantGroupData, studyId, groupId, inputDataType, data )
}
