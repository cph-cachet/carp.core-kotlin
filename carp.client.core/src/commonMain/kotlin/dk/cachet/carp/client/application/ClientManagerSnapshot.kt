package dk.cachet.carp.client.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.UUIDSerializer
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


@Serializable
data class ClientManagerSnapshot(
    @Serializable( with = DeviceRegistrationSerializer::class )
    val deviceRegistration: DeviceRegistration,
    val studies: List<Pair<@Serializable( with = UUIDSerializer::class ) UUID, String>> )
{
    companion object
    {
        /**
         * Create a snapshot of the specified [ClientManager].
         *
         * @param clientManager The [ClientManager] to create a snapshot for.
         */
        fun fromClientManager( clientManager: ClientManager<*, *> ): ClientManagerSnapshot
        {
            val studyRuntimes = clientManager.studies.map { Pair( it.studyDeploymentId, it.deviceRoleName ) }

            return ClientManagerSnapshot( clientManager.deviceRegistration, studyRuntimes )
        }
    }
}