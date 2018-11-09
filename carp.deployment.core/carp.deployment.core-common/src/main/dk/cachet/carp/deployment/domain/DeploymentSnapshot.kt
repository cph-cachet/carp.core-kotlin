package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.JSON


/**
 * Custom serializer for a map containing [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
private object RegisteredDevicesSerializer : KSerializer<Map<String, DeviceRegistration>> by HashMapSerializer( StringSerializer, DeviceRegistrationSerializer )


/**
 * A serializable snapshot of a [Deployment] at the moment in time when it was created.
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
         * Create a snapshot of the specified [Deployment].
         *
         * @param deployment The [Deployment] to create a snapshot for.
         */
        fun fromDeployment( deployment: Deployment ): DeploymentSnapshot
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
            return JSON.parse( json )
        }
    }


    /**
     * Serialize to JSON using the built-in serializer.
     */
    fun toJson(): String
    {
        return JSON.stringify( this )
    }
}