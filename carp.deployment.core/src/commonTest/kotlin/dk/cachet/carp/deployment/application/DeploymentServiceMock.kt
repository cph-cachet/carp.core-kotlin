package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.StudyProtocolSnapshot
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.test.Mock

private typealias Service = DeploymentService


class DeploymentServiceMock(
    private val createStudyDeploymentResult: StudyDeploymentStatus = emptyStatus,
    private val removeStudyDeploymentsResult: Set<UUID> = emptySet(),
    private val getStudyDeploymentStatusResult: StudyDeploymentStatus = emptyStatus,
    private val getStudyDeploymentStatusListResult: List<StudyDeploymentStatus> = emptyList(),
    private val registerDeviceResult: StudyDeploymentStatus = emptyStatus,
    private val unregisterDeviceResult: StudyDeploymentStatus = emptyStatus,
    private val getDeviceDeploymentForResult: MasterDeviceDeployment = emptyMasterDeviceDeployment,
    private val deploymentSuccessfulResult: StudyDeploymentStatus = emptyStatus,
    private val stopResult: StudyDeploymentStatus = emptyStatus
) : Mock<Service>(), Service
{
    companion object
    {
        private val emptyStatus: StudyDeploymentStatus = StudyDeploymentStatus.DeployingDevices(
            UUID( "00000000-0000-0000-0000-000000000000"),
            listOf(), null )
        private val emptyMasterDeviceDeployment: MasterDeviceDeployment = MasterDeviceDeployment(
            StubMasterDeviceDescriptor(),
            DefaultDeviceRegistration( "Test" ),
            setOf(), mapOf(), setOf(), mapOf(), setOf() )
    }


    override suspend fun createStudyDeployment( protocol: StudyProtocolSnapshot ) =
        createStudyDeploymentResult
        .also { trackSuspendCall( Service::createStudyDeployment, protocol ) }

    override suspend fun removeStudyDeployments( studyDeploymentIds: Set<UUID> ) =
        removeStudyDeploymentsResult
        .also { trackSuspendCall( Service::removeStudyDeployments, studyDeploymentIds ) }

    override suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ) =
        getStudyDeploymentStatusResult
        .also { trackSuspendCall( Service::getStudyDeploymentStatus, studyDeploymentId ) }

    override suspend fun getStudyDeploymentStatusList( studyDeploymentIds: Set<UUID> ) =
        getStudyDeploymentStatusListResult
        .also { trackSuspendCall( Service::getStudyDeploymentStatusList, studyDeploymentIds ) }

    override suspend fun registerDevice( studyDeploymentId: UUID, deviceRoleName: String, registration: DeviceRegistration ) =
        registerDeviceResult
        .also { trackSuspendCall( Service::registerDevice, studyDeploymentId, deviceRoleName, registration ) }

    override suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ) =
        unregisterDeviceResult
        .also { trackSuspendCall( Service::unregisterDevice, studyDeploymentId, deviceRoleName ) }

    override suspend fun getDeviceDeploymentFor( studyDeploymentId: UUID, masterDeviceRoleName: String ) =
        getDeviceDeploymentForResult
        .also { trackSuspendCall( Service::getDeviceDeploymentFor, studyDeploymentId, masterDeviceRoleName ) }

    override suspend fun deploymentSuccessful( studyDeploymentId: UUID, masterDeviceRoleName: String, deviceDeploymentLastUpdateDate: DateTime ) =
        deploymentSuccessfulResult
        .also { trackSuspendCall( Service::deploymentSuccessful, studyDeploymentId, masterDeviceRoleName, deviceDeploymentLastUpdateDate ) }

    override suspend fun stop( studyDeploymentId: UUID ) =
        stopResult
        .also { trackSuspendCall( Service::stop, studyDeploymentId ) }
}
