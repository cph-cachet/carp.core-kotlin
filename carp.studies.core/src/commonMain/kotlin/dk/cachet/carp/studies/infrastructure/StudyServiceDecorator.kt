package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyStatus


@Suppress( "TooManyFunctions" )
class StudyServiceDecorator(
    service: StudyService,
    requestDecorator: (Command<StudyServiceRequest<*>>) -> Command<StudyServiceRequest<*>>
) : ApplicationServiceDecorator<StudyService, StudyServiceRequest<*>>( service, requestDecorator ),
    StudyService
{
    override suspend fun createStudy(
        ownerId: UUID,
        name: String,
        description: String?,
        invitation: StudyInvitation?
    ): StudyStatus = invoke(
        StudyServiceRequest.CreateStudy( ownerId, name, description, invitation )
    )

    override suspend fun setInternalDescription(
        studyId: UUID,
        name: String,
        description: String?
    ): StudyStatus = invoke(
        StudyServiceRequest.SetInternalDescription( studyId, name, description )
    )

    override suspend fun getStudyDetails( studyId: UUID ): StudyDetails = invoke(
        StudyServiceRequest.GetStudyDetails( studyId )
    )

    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus = invoke(
        StudyServiceRequest.GetStudyStatus( studyId )
    )

    override suspend fun getStudiesOverview( ownerId: UUID ): List<StudyStatus> = invoke(
        StudyServiceRequest.GetStudiesOverview( ownerId )
    )

    override suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ): StudyStatus = invoke(
        StudyServiceRequest.SetInvitation( studyId, invitation )
    )

    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ): StudyStatus = invoke(
        StudyServiceRequest.SetProtocol( studyId, protocol )
    )

    override suspend fun removeProtocol( studyId: UUID ): StudyStatus = invoke(
        StudyServiceRequest.RemoveProtocol( studyId )
    )

    override suspend fun goLive( studyId: UUID ): StudyStatus = invoke(
        StudyServiceRequest.GoLive( studyId )
    )

    override suspend fun remove( studyId: UUID ): Boolean = invoke(
        StudyServiceRequest.Remove( studyId )
    )

    override suspend fun StudyServiceRequest<*>.invokeOnService( service: StudyService ): Any? =
        when ( this )
        {
            is StudyServiceRequest.CreateStudy -> service.createStudy( ownerId, name, description, invitation )
            is StudyServiceRequest.SetInternalDescription ->
                service.setInternalDescription( studyId, name, description )
            is StudyServiceRequest.GetStudyDetails -> service.getStudyDetails( studyId )
            is StudyServiceRequest.GetStudyStatus -> service.getStudyStatus( studyId )
            is StudyServiceRequest.GetStudiesOverview -> service.getStudiesOverview( ownerId )
            is StudyServiceRequest.SetInvitation -> service.setInvitation( studyId, invitation )
            is StudyServiceRequest.SetProtocol -> service.setProtocol( studyId, protocol )
            is StudyServiceRequest.RemoveProtocol -> service.removeProtocol( studyId )
            is StudyServiceRequest.GoLive -> service.goLive( studyId )
            is StudyServiceRequest.Remove -> service.remove( studyId )
        }
}
