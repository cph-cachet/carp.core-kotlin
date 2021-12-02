

package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.toEpochMicroseconds


@JsModule( "big.js" )
@JsNonModule
@Suppress( "FunctionName" )
external fun Big( number: Number ): dynamic


actual fun SyncPoint.applyToTimestamp( timestamp: Long ): Long
{
    val excludingEpoch = Big( relativeClockSpeed ).times(
        Big( timestamp - sensorTimestampAtSyncPoint )
    ).toFixed() as String

    return excludingEpoch.toLong() + synchronizedOn.toEpochMicroseconds()
}
