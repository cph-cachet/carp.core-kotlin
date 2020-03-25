package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.domain.StudyDetails
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [StudyService] which can be executed on demand.
 */
@Serializable
sealed class StudyServiceRequest
{
    @Serializable
    data class CreateStudy( val owner: StudyOwner, val name: String, val description: String = "", val invitation: StudyInvitation? = null ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::createStudy, owner, name, description, invitation )

    @Serializable
    data class SetInternalDescription( val studyId: UUID, val name: String, val description: String ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::setInternalDescription, studyId, name, description )

    @Serializable
    data class GetStudyDetails( val studyId: UUID ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyDetails> by createServiceInvoker( StudyService::getStudyDetails, studyId )

    @Serializable
    data class GetStudyStatus( val studyId: UUID ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::getStudyStatus, studyId )

    @Serializable
    data class GetStudiesOverview( val owner: StudyOwner ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, List<StudyStatus>> by createServiceInvoker( StudyService::getStudiesOverview, owner )

    @Serializable
    data class AddParticipant( val studyId: UUID, val email: EmailAddress ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, Participant> by createServiceInvoker( StudyService::addParticipant, studyId, email )

    @Serializable
    data class GetParticipants( val studyId: UUID ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, List<Participant>> by createServiceInvoker( StudyService::getParticipants, studyId )

    @Serializable
    data class SetInvitation( val studyId: UUID, val invitation: StudyInvitation ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::setInvitation, studyId, invitation )

    @Serializable
    data class SetProtocol( val studyId: UUID, val protocol: StudyProtocolSnapshot ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::setProtocol, studyId, protocol )

    @Serializable
    data class GoLive( val studyId: UUID ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::goLive, studyId )

    @Serializable
    data class DeployParticipantGroup( val studyId: UUID, val group: Set<AssignParticipantDevices> ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::deployParticipantGroup, studyId, group )
}
