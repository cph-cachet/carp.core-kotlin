package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.domain.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.StudyDetails
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import kotlinx.serialization.Serializable

private typealias Service = StudyService
private typealias Invoker<T> = ServiceInvoker<StudyService, T>


/**
 * Serializable application service requests to [StudyService] which can be executed on demand.
 */
@Serializable
sealed class StudyServiceRequest
{
    @Serializable
    data class CreateStudy( val owner: StudyOwner, val name: String, val description: String = "", val invitation: StudyInvitation? = null ) :
        StudyServiceRequest(),
        Invoker<StudyStatus> by createServiceInvoker( Service::createStudy, owner, name, description, invitation )

    @Serializable
    data class SetInternalDescription( val studyId: UUID, val name: String, val description: String ) :
        StudyServiceRequest(),
        Invoker<StudyStatus> by createServiceInvoker( Service::setInternalDescription, studyId, name, description )

    @Serializable
    data class GetStudyDetails( val studyId: UUID ) :
        StudyServiceRequest(),
        Invoker<StudyDetails> by createServiceInvoker( Service::getStudyDetails, studyId )

    @Serializable
    data class GetStudyStatus( val studyId: UUID ) :
        StudyServiceRequest(),
        Invoker<StudyStatus> by createServiceInvoker( Service::getStudyStatus, studyId )

    @Serializable
    data class GetStudiesOverview( val owner: StudyOwner ) :
        StudyServiceRequest(),
        Invoker<List<StudyStatus>> by createServiceInvoker( Service::getStudiesOverview, owner )

    @Serializable
    data class AddParticipant( val studyId: UUID, val email: EmailAddress ) :
        StudyServiceRequest(),
        Invoker<Participant> by createServiceInvoker( Service::addParticipant, studyId, email )

    @Serializable
    data class GetParticipants( val studyId: UUID ) :
        StudyServiceRequest(),
        Invoker<List<Participant>> by createServiceInvoker( Service::getParticipants, studyId )

    @Serializable
    data class SetInvitation( val studyId: UUID, val invitation: StudyInvitation ) :
        StudyServiceRequest(),
        Invoker<StudyStatus> by createServiceInvoker( Service::setInvitation, studyId, invitation )

    @Serializable
    data class SetProtocol( val studyId: UUID, val protocol: StudyProtocolSnapshot ) :
        StudyServiceRequest(),
        Invoker<StudyStatus> by createServiceInvoker( Service::setProtocol, studyId, protocol )

    @Serializable
    data class GoLive( val studyId: UUID ) :
        StudyServiceRequest(),
        Invoker<StudyStatus> by createServiceInvoker( Service::goLive, studyId )

    @Serializable
    data class DeployParticipantGroup( val studyId: UUID, val group: Set<AssignParticipantDevices> ) :
        StudyServiceRequest(),
        Invoker<ParticipantGroupStatus> by createServiceInvoker( Service::deployParticipantGroup, studyId, group )

    @Serializable
    data class GetParticipantGroupStatuses( val studyId: UUID ) :
        StudyServiceRequest(),
        Invoker<List<ParticipantGroupStatus>> by createServiceInvoker( Service::getParticipantGroupStatuses, studyId )
}
