package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.Snapshot
import dk.cachet.carp.deployment.domain.users.AccountParticipation
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationSerializer
import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [StudyDeployment] at the moment in time when it was created.
 */
@Serializable
data class StudyDeploymentSnapshot(
    val studyDeploymentId: UUID,
    val studyProtocolSnapshot: StudyProtocolSnapshot,
    val registeredDevices: Set<String>,
    val deviceRegistrationHistory: Map<String, List<@Serializable( DeviceRegistrationSerializer::class ) DeviceRegistration>>,
    val deployedDevices: Set<String>,
    val invalidatedDeployedDevices: Set<String>,
    val hasStopped: Boolean,
    val participations: Set<AccountParticipation>
) : Snapshot<StudyDeployment>()
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
                studyDeployment.protocolSnapshot,
                studyDeployment.registeredDevices.map { it.key.roleName }.toSet(),
                studyDeployment.deviceRegistrationHistory.mapKeys { it.key.roleName },
                studyDeployment.deployedDevices.map { it.roleName }.toSet(),
                studyDeployment.invalidatedDeployedDevices.map { it.roleName }.toSet(),
                studyDeployment.hasStopped,
                studyDeployment.participations )
        }
    }


    override fun toObject(): StudyDeployment = StudyDeployment.fromSnapshot( this )
}
