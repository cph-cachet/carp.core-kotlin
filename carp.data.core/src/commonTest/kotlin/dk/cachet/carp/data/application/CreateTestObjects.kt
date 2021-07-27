package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.datetime.Clock


val stubDeploymentId = UUID.randomUUID()
val stubSyncPoint = SyncPoint( Clock.System.now() )
val stubTriggerIds = listOf( 1 )

/**
 * Create a [DataStreamSequence], always using the same data stream, except for the data type defined by [data].
 */
inline fun <reified T : Data> createStubSequence( firstSequenceId: Long, vararg data: T ): DataStreamSequence
{
    val sequence = MutableDataStreamSequence(
        dataStreamId<T>( stubDeploymentId, "Device" ),
        firstSequenceId,
        stubTriggerIds,
        stubSyncPoint
    )

    val measurements = data.map { measurement( it, 0 ) }
    sequence.appendMeasurements( measurements )

    return sequence
}
