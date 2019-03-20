package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


/**
 * A serializable snapshot of a [StudyDeployment] at the moment in time when it was created.
 */
@Serializable
data class DeploymentSnapshot(
    val deploymentId: String,
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
        fun fromDeployment( deployment: StudyDeployment ): DeploymentSnapshot
        {
            return DeploymentSnapshot(
                deployment.id.toString(),
                deployment.protocolSnapshot,
                deployment.registeredDevices.mapKeys { it.key.roleName } )
        }

        /**
         * Create a snapshot from JSON serialized using the built-in serializer.
         *
         * @param json The JSON which was serialized using the built-in serializer (`DeploymentSnapshot.toJson`).
         */
        fun fromJson( json: String ): DeploymentSnapshot
        {
            return Json.parse( DeploymentSnapshot.serializer(), json )
        }
    }


    /**
     * Serialize to JSON using the built-in serializer.
     */
    fun toJson(): String
    {
        return Json.stringify( DeploymentSnapshot.serializer(), this )
    }
}