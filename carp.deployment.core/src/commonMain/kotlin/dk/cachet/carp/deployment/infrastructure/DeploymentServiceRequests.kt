package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.*
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.*


/**
 * Serializable application service requests to [ProtocolService] which can be executed on demand.
 */
@Polymorphic
@Serializable
abstract class DeploymentServiceRequest
{
    @Serializable
    data class CreateStudyDeployment( val protocol: StudyProtocolSnapshot ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::createStudyDeployment, protocol )

    @Serializable
    data class GetStudyDeploymentStatus( val studyDeploymentId: UUID ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::getStudyDeploymentStatus, studyDeploymentId )

    @Serializable
    data class RegisterDevice(
        val studyDeploymentId: UUID,
        val deviceRoleName: String,
        @Serializable( DeviceRegistrationSerializer::class )
        val registration: DeviceRegistration ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, StudyDeploymentStatus> by createServiceInvoker( DeploymentService::registerDevice, studyDeploymentId, deviceRoleName, registration )

    @Serializable
    data class GetDeviceDeploymentFor( val studyDeploymentId: UUID, val masterDeviceRoleName: String ) :
        DeploymentServiceRequest(),
        ServiceInvoker<DeploymentService, MasterDeviceDeployment> by createServiceInvoker( DeploymentService::getDeviceDeploymentFor, studyDeploymentId, masterDeviceRoleName )
}