package dk.cachet.carp.data.application


import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Serializable


/**
 * Store and retrieve [DataStreamPoint]s for study deployments.
 *
 * TODO: When one of the devices in a deployment is unregistered, the deployment is no longer "ready".
 *   We should likely introduce an intermediate state "consented", starting from which data may be uploaded.
 */
interface DataStreamService : ApplicationService<DataStreamService, DataStreamService.Event>
{
    @Serializable
    sealed class Event : IntegrationEvent<DataStreamService>()

    /**
     * Append a [batch] of data point sequences to corresponding data streams in [studyDeploymentId].
     *
     * @throws IllegalArgumentException when the start of any of the sequences contained in [batch]
     *   precede the end of a previously appended sequence to the same data stream.
     */
    suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch )

    /**
     * Retrieve all data points in [dataStream] that fall within the inclusive range
     * defined by [fromSequenceId] and [toSequenceIdInclusive].
     * If [toSequenceIdInclusive] is null, all data points starting [fromSequenceId] are returned.
     *
     * In case no data for [dataStream] is stored in this repository, or is available for the specified range,
     * an empty [DataStreamBatch] is returned.
     */
    suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long? = null
    ): DataStreamBatch
}
