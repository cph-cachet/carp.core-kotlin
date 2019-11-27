package dk.cachet.carp.client.domain

import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


@Serializable
data class ClientManagerSnapshot(
    @Serializable( with = DeviceRegistrationSerializer::class )
    val deviceRegistration: DeviceRegistration,
    val studies: List<StudyRuntimeSnapshot> )
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
            val studyRuntimes = clientManager.studies.map { StudyRuntimeSnapshot.fromStudyRuntime( it ) }

            return ClientManagerSnapshot( clientManager.deviceRegistration, studyRuntimes )
        }
    }
}
