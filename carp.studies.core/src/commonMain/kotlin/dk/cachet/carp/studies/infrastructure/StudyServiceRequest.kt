package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [StudyService] which can be executed on demand.
 */
@Serializable
sealed class StudyServiceRequest
{
    @Serializable
    data class CreateStudy( val owner: StudyOwner, val name: String, val invitation: StudyInvitation? = null ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::createStudy, owner, name, invitation )

    @Serializable
    data class GetStudyStatus( val studyId: UUID ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::getStudyStatus, studyId )
}
