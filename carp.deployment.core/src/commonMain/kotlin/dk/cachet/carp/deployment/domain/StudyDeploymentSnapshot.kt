package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


/**
 * A serializable snapshot of a [StudyDeployment] at the moment in time when it was created.
 */
@Serializable
data class StudyDeploymentSnapshot(
    @Serializable( with = UUIDSerializer::class )
    val deploymentId: UUID,
    val studyProtocolSnapshot: StudyProtocolSnapshot,
    @Serializable( RegisteredDevicesSerializer::class )
    val registeredDevices: Map<String, DeviceRegistration> )
{
    companion object
    {
        /**
         * Create a snapshot of the specified [StudyDeployment].
         *
         * @param deployment The [StudyDeployment] to create a snapshot for.
         */
        fun fromDeployment( deployment: StudyDeployment ): StudyDeploymentSnapshot
        {
            return StudyDeploymentSnapshot(
                deployment.id,
                deployment.protocolSnapshot,
                deployment.registeredDevices.mapKeys { it.key.roleName } )
        }
    }
}