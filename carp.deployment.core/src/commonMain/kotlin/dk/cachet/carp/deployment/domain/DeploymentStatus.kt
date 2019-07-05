package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.serialization.JSON
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [StudyDeployment]: registered devices, last received data, whether consent has been given, etc.
 */
@Serializable
data class DeploymentStatus(
    val deploymentId: String,
    /**
     * The set of all devices which can or need to be registered for this deployment.
     */
    val registrableDevices: Set<RegistrableDevice>,
    /**
     * The role names of all [registrableDevices] which still require registration for the deployment to start running.
     */
    val remainingDevicesToRegister: Set<String>,
    /**
     * The role names of all devices which have been registered successfully and are ready for deployment.
     */
    val devicesReadyForDeployment: Set<String> )
{
    companion object {
        /**
         * Create a snapshot from JSON serialized using the built-in serializer.
         *
         * @param json The JSON which was serialized using the built-in serializer (`DeploymentStatus.toJson`).
         */
        fun fromJson( json: String ): DeploymentStatus
        {
            return JSON.parse( serializer(), json )
        }
    }


    /**
     * Serialize to JSON using the built-in serializer.
     */
    fun toJson(): String
    {
        return JSON.stringify( serializer(),this )
    }
}