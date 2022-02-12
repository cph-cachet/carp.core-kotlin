package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


/**
 * Serializable application service requests to [StudyService] which can be executed on demand.
 */
@Serializable
sealed class StudyServiceRequest<out TReturn> : ApplicationServiceRequest<StudyService, TReturn>
{
    @Required
    override val apiVersion: ApiVersion = StudyService.API_VERSION

    object Serializer : KSerializer<StudyServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class CreateStudy(
        val ownerId: UUID,
        val name: String,
        val description: String? = null,
        val invitation: StudyInvitation? = null
    ) : StudyServiceRequest<StudyStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyStatus>()
        override suspend fun invokeOn( service: StudyService ) =
            service.createStudy( ownerId, name, description, invitation )
    }

    @Serializable
    data class SetInternalDescription( val studyId: UUID, val name: String, val description: String ) :
        StudyServiceRequest<StudyStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyStatus>()
        override suspend fun invokeOn( service: StudyService ) =
            service.setInternalDescription( studyId, name, description )
    }

    @Serializable
    data class GetStudyDetails( val studyId: UUID ) : StudyServiceRequest<StudyDetails>()
    {
        override fun getResponseSerializer() = serializer<StudyDetails>()
        override suspend fun invokeOn( service: StudyService ) = service.getStudyDetails( studyId )
    }

    @Serializable
    data class GetStudyStatus( val studyId: UUID ) : StudyServiceRequest<StudyStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyStatus>()
        override suspend fun invokeOn( service: StudyService ) = service.getStudyStatus( studyId )
    }

    @Serializable
    data class GetStudiesOverview( val ownerId: UUID ) : StudyServiceRequest<List<StudyStatus>>()
    {
        override fun getResponseSerializer() = serializer<List<StudyStatus>>()
        override suspend fun invokeOn( service: StudyService ) = service.getStudiesOverview( ownerId )
    }

    @Serializable
    data class SetInvitation( val studyId: UUID, val invitation: StudyInvitation ) : StudyServiceRequest<StudyStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyStatus>()
        override suspend fun invokeOn( service: StudyService ) = service.setInvitation( studyId, invitation )
    }

    @Serializable
    data class SetProtocol( val studyId: UUID, val protocol: StudyProtocolSnapshot ) :
        StudyServiceRequest<StudyStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyStatus>()
        override suspend fun invokeOn( service: StudyService ) = service.setProtocol( studyId, protocol )
    }

    @Serializable
    data class GoLive( val studyId: UUID ) : StudyServiceRequest<StudyStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyStatus>()
        override suspend fun invokeOn( service: StudyService ) = service.goLive( studyId )
    }

    @Serializable
    data class Remove( val studyId: UUID ) : StudyServiceRequest<Boolean>()
    {
        override fun getResponseSerializer() = serializer<Boolean>()
        override suspend fun invokeOn( service: StudyService ) = service.remove( studyId )
    }
}
