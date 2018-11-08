package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.*


/**
 * Custom serializer for a map containing [DeviceRegistration] values which may be extending types.
 */
private object RegisteredDevicesSerializer : KSerializer<Map<String, Any>> by HashMapSerializer( StringSerializer, PolymorphicSerializer )


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
    }
}