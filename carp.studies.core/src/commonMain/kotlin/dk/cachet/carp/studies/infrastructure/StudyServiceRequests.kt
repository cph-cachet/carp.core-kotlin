@file:UseSerializers( UUIDSerializer::class )

package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.*
import dk.cachet.carp.common.ddd.*
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.domain.*
import kotlinx.serialization.*


/**
 * Serializable application service requests to [StudyService] which can be executed on demand.
 */
@Polymorphic
@Serializable
abstract class StudyServiceRequest
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