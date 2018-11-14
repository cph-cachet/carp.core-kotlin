package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON


/**
 * Contains the entire description and configuration for how a single device participates in running a study.
 */
@Serializable
data class DeviceDeployment(
    /**
     * Configuration for this device.
     */
    val configuration: DeviceRegistration,
    /**
     * The devices this device needs to connect to.
     */
    val connectedDevices: List<DeviceDescriptor>,
    /**
     * Preregistration of connected devices, including configuration such as connection properties, stored per role name.
     */
    val connectedDeviceConfigurations: Map<String, DeviceRegistration> )
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