package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.toEpochMicroseconds
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.datetime.Clock


val stubDeploymentId = UUID.randomUUID()
val stubSyncPoint = SyncPoint( Clock.System.now() )
val stubTriggerIds = listOf( 1 )
const val stubSequenceDeviceRoleName = "Device"

/**
 * Create a [DataStreamSequence], always using the same data stream identifiers, except for the data type defined by [T].
 */
inline fun <reified T : Data> createStubSequence( firstSequenceId: Long, vararg data: T ): DataStreamSequence =
    createStubSequence(
        firstSequenceId,
        *data.map { measurement( it, 0 ) }.toTypedArray()
    )

/**
 * Create a [DataStreamSequence], always using the same data stream identifiers, except for the data type defined by [T].
 */
inline fun <reified T : Data> createStubSequence(
    firstSequenceId: Long,
    vararg measurements: Measurement<T>
): DataStreamSequence =
    MutableDataStreamSequence(
        dataStreamId<T>( stubDeploymentId, stubSequenceDeviceRoleName ),
        firstSequenceId,
        stubTriggerIds,
        stubSyncPoint
    ).apply { appendMeasurements( measurements.toList() ) }

/**
 * Create a [SyncPoint] for the current time for a clock which runs twice as fast as UTC time.
 */
fun createDoubleSpeedSyncPoint(): SyncPoint
{
    val now = Clock.System.now()
    val nowMicros = now.toEpochMicroseconds()

    return SyncPoint(
        now,
        nowMicros * 2,
        0.5
    )
}
