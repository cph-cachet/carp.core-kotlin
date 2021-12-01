package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.toEpochMicroseconds


actual fun SyncPoint.applyToTimestamp( timestamp: Long ): Long
{
    val excludingEpoch = relativeClockSpeed.toBigDecimal() *
        (timestamp - sensorTimestampAtSyncPoint).toBigDecimal()

    return excludingEpoch.toLong() + synchronizedOn.toEpochMicroseconds()
}
