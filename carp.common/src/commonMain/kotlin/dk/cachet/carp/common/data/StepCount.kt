package dk.cachet.carp.common.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds step count data as number of steps taken in a corresponding time interval.
 */
@Serializable
@SerialName( CarpDataTypes.STEPCOUNT_TYPE_NAME )
data class StepCount( val steps: Int ) : Data
{
    init
    {
        require( steps >= 0 ) { "Number of steps needs to be a positive number." }
    }
}
