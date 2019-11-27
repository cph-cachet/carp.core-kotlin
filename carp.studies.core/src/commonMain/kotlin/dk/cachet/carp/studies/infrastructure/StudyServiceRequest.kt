package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.createServiceInvoker
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.domain.StudyDescription
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudyStatus
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * Serializable application service requests to [StudyService] which can be executed on demand.
 */
@Polymorphic
@Serializable
sealed class StudyServiceRequest
{
    @Serializable
    data class CreateStudy( val owner: StudyOwner, val name: String, val description: StudyDescription? = null ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::createStudy, owner, name, description )

    @Serializable
    data class GetStudyStatus( val studyId: UUID ) :
        StudyServiceRequest(),
        ServiceInvoker<StudyService, StudyStatus> by createServiceInvoker( StudyService::getStudyStatus, studyId )
}
