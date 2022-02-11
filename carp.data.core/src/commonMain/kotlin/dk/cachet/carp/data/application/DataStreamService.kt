package dk.cachet.carp.data.application


import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Store and retrieve [DataStreamPoint]s for study deployments.
 */
interface DataStreamService : ApplicationService<DataStreamService, DataStreamService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 0 ) }

    @Serializable
    sealed class Event : IntegrationEvent<DataStreamService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }


    /**
     * Start accepting data for a study deployment for data streams configured in [configuration].
     *
     * @throws IllegalStateException when data streams for the specified study deployment have already been configured.
     */
    suspend fun openDataStreams( configuration: DataStreamsConfiguration )

    /**
     * Append a [batch] of data point sequences to corresponding data streams in [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     *  - the `studyDeploymentId` of one or more sequences in [batch] does not match [studyDeploymentId]
     *  - the start of one or more of the sequences contained in [batch]
     *  precede the end of a previously appended sequence to the same data stream
     *  - [batch] contains a sequence with [DataStreamId] which wasn't configured for [studyDeploymentId]
     * @throws IllegalStateException when data streams for [studyDeploymentId] have been closed.
     */
    suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch )

    /**
     * Retrieve all data points in [dataStream] that fall within the inclusive range
     * defined by [fromSequenceId] and [toSequenceIdInclusive].
     * If [toSequenceIdInclusive] is null, all data points starting [fromSequenceId] are returned.
     *
     * In case no data for [dataStream] is stored in this repository, or is available for the specified range,
     * an empty [DataStreamBatch] is returned.
     *
     * @throws IllegalArgumentException when [dataStream] has never been opened.
     */
    suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long? = null
    ): DataStreamBatch

    /**
     * Stop accepting incoming data for all data streams for each of the [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when no data streams were ever opened for any of the [studyDeploymentIds].
     */
    suspend fun closeDataStreams( studyDeploymentIds: Set<UUID> )

    /**
     * Close data streams and remove all data for each of the [studyDeploymentIds].
     *
     * @return True when any data streams have been removed, or false when there were no data streams to remove.
     */
    suspend fun removeDataStreams( studyDeploymentIds: Set<UUID> ): Boolean
}
