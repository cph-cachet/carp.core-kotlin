package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.ClientRepository
import dk.cachet.carp.client.domain.StudyRuntime
import dk.cachet.carp.client.domain.StudyRuntimeSnapshot
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


/**
 * A [ClientRepository] which holds [StudyRuntime]s in memory as long as the instance is held in memory.
 */
class InMemoryClientRepository(
    /**
     * The deployment service used to initialize [StudyRuntime] when they are retrieved from the repository.
     */
    val deploymentService: DeploymentService
) : ClientRepository
{
    /**
     * The [DeviceRegistration] used to register the client in deployments.
     */
    override var deviceRegistration: DeviceRegistration? = null

    private val studyRuntimes: MutableList<StudyRuntimeSnapshot> = mutableListOf()


    /**
     * Adds the specified [studyRuntime] to the repository.
     *
     * @throws IllegalArgumentException when a [StudyRuntime] which has the same study deployment ID and device role name already exists.
     */
    override fun addStudyRuntime( studyRuntime: StudyRuntime )
    {
        val deploymentId = studyRuntime.studyDeploymentId
        val deviceRoleName = studyRuntime.device.roleName
        require( studyRuntimes.none { it.studyDeploymentId == deploymentId && it.device.roleName == deviceRoleName } )

        studyRuntimes.add( studyRuntime.getSnapshot() )
    }

    /**
     * Return the [StudyRuntime] with [studyDeploymentId] and [deviceRoleName], or null when no such [StudyRuntime] is found.
     */
    override fun getStudyRuntimeBy( studyDeploymentId: UUID, deviceRoleName: String ): StudyRuntime? =
        studyRuntimes
            .filter { it.studyDeploymentId == studyDeploymentId && it.device.roleName == deviceRoleName }
            .map { StudyRuntime.fromSnapshot( it, deploymentService ) }
            .firstOrNull()

    /**
     * Return all [StudyRuntime]s for the client.
     */
    override fun getStudyRuntimeList(): List<StudyRuntime> =
        studyRuntimes.map { StudyRuntime.fromSnapshot( it, deploymentService ) }
}
