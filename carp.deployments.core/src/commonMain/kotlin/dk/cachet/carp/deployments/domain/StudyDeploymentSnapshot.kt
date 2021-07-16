package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [StudyDeployment] at the moment in time when it was created.
 */
@Serializable
data class StudyDeploymentSnapshot(
    val studyDeploymentId: UUID,
    override val creationDate: Instant,
    val studyProtocolSnapshot: StudyProtocolSnapshot,
    val registeredDevices: Set<String>,
    val deviceRegistrationHistory: Map<String, List<DeviceRegistration>>,
    val deployedDevices: Set<String>,
    val invalidatedDeployedDevices: Set<String>,
    val startTime: Instant?,
    val isStopped: Boolean
) : Snapshot<StudyDeployment>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [StudyDeployment].
         *
         * @param studyDeployment The [StudyDeployment] to create a snapshot for.
         */
        fun fromDeployment( studyDeployment: StudyDeployment ): StudyDeploymentSnapshot
        {
            return StudyDeploymentSnapshot(
                studyDeployment.id,
                studyDeployment.creationDate,
                studyDeployment.protocolSnapshot,
                studyDeployment.registeredDevices.map { it.key.roleName }.toSet(),
                studyDeployment.deviceRegistrationHistory.mapKeys { it.key.roleName },
                studyDeployment.deployedDevices.map { it.roleName }.toSet(),
                studyDeployment.invalidatedDeployedDevices.map { it.roleName }.toSet(),
                studyDeployment.startTime,
                studyDeployment.isStopped )
        }
    }


    override fun toObject(): StudyDeployment = StudyDeployment.fromSnapshot( this )
}
