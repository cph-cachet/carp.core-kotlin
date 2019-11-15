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
    val studyDeploymentId: UUID,
    val studyProtocolSnapshot: StudyProtocolSnapshot,
    val registeredDevices: Map<String, @Serializable( DeviceRegistrationSerializer::class ) DeviceRegistration> )
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
                studyDeployment.registeredDevices.mapKeys { it.key.roleName } )
        }
    }
}