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
inline fun <reified T : Data> createStubSequence( firstSequenceId: Long, vararg data: T ): DataStreamSequence<T> =
    DataStreamSequence.fromMeasurements(
        dataStreamId<T>( stubDeploymentId, "Device" ),
        firstSequenceId,
        data.map { measurement( it, 0 ) },
        stubTriggerIds,
        stubSyncPoint
    )
