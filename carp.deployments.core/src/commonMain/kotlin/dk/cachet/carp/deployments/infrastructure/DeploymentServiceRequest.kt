package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.datetime.Instant
import kotlinx.serialization.*


/**
 * Serializable application service requests to [DeploymentService] which can be executed on demand.
 */
@Serializable
sealed class DeploymentServiceRequest<out TReturn> : ApplicationServiceRequest<DeploymentService, TReturn>
{
    @Required
    override val apiVersion: ApiVersion = DeploymentService.API_VERSION

    object Serializer : KSerializer<DeploymentServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class CreateStudyDeployment(
        val id: UUID,
        val protocol: StudyProtocolSnapshot,
        val invitations: List<ParticipantInvitation>,
        val connectedDevicePreregistrations: Map<String, DeviceRegistration> = emptyMap()
    ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.createStudyDeployment( id, protocol, invitations, connectedDevicePreregistrations )
    }

    @Serializable
    data class RemoveStudyDeployments( val studyDeploymentIds: Set<UUID> ) : DeploymentServiceRequest<Set<UUID>>()
    {
        override fun getResponseSerializer() = serializer<Set<UUID>>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.removeStudyDeployments( studyDeploymentIds )
    }

    @Serializable
    data class GetStudyDeploymentStatus( val studyDeploymentId: UUID ) :
        DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.getStudyDeploymentStatus( studyDeploymentId )
    }

    @Serializable
    data class GetStudyDeploymentStatusList( val studyDeploymentIds: Set<UUID> ) :
        DeploymentServiceRequest<List<StudyDeploymentStatus>>()
    {
        override fun getResponseSerializer() = serializer<List<StudyDeploymentStatus>>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.getStudyDeploymentStatusList( studyDeploymentIds )
    }

    @Serializable
    data class RegisterDevice(
        val studyDeploymentId: UUID,
        val deviceRoleName: String,
        val registration: DeviceRegistration
    ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.registerDevice( studyDeploymentId, deviceRoleName, registration )
    }

    @Serializable
    data class UnregisterDevice( val studyDeploymentId: UUID, val deviceRoleName: String ) :
        DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.unregisterDevice( studyDeploymentId, deviceRoleName )
    }

    @Serializable
    data class GetDeviceDeploymentFor( val studyDeploymentId: UUID, val primaryDeviceRoleName: String ) :
        DeploymentServiceRequest<PrimaryDeviceDeployment>()
    {
        override fun getResponseSerializer() = serializer<PrimaryDeviceDeployment>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.getDeviceDeploymentFor( studyDeploymentId, primaryDeviceRoleName )
    }

    @Serializable
    data class DeviceDeployed(
        val studyDeploymentId: UUID,
        val primaryDeviceRoleName: String,
        val deviceDeploymentLastUpdatedOn: Instant
    ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
        override suspend fun invokeOn( service: DeploymentService ) =
            service.deviceDeployed( studyDeploymentId, primaryDeviceRoleName, deviceDeploymentLastUpdatedOn )
    }

    @Serializable
    data class Stop( val studyDeploymentId: UUID ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
        override suspend fun invokeOn( service: DeploymentService ) = service.stop( studyDeploymentId )
    }
}
