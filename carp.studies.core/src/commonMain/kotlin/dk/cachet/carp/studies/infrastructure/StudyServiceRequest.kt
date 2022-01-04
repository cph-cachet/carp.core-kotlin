package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.services.createServiceInvoker
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyStatus
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
    data class CreateStudy(
        val ownerId: UUID,
        val name: String,
        val description: String? = null,
        val invitation: StudyInvitation? = null
    ) : StudyServiceRequest(),
        Invoker<StudyStatus> by createServiceInvoker( Service::createStudy, ownerId, name, description, invitation )

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
    data class GetStudiesOverview( val ownerId: UUID ) :
        StudyServiceRequest(),
        Invoker<List<StudyStatus>> by createServiceInvoker( Service::getStudiesOverview, ownerId )

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
    data class Remove( val studyId: UUID ) :
        StudyServiceRequest(),
        Invoker<Boolean> by createServiceInvoker( Service::remove, studyId )
}
