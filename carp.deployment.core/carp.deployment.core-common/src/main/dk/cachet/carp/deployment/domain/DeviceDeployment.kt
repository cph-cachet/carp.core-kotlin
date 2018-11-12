package dk.cachet.carp.deployment.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON


/**
 * Contains the entire description and configuration for how a single device participates in running a study.
 */
@Serializable
data class DeviceDeployment(
    val deploymentId: String )
{
    companion object
    {
        /**
         * Create a [DeviceDeployment] from JSON serialized using the built-in serializer.
         *
         * @param json The JSON which was serialized using the built-in serializer (`DeviceDeployment.toJson`).
         */
        fun fromJson( json: String ): DeviceDeployment
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