@file:JsExport

package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Holds step count data as number of steps taken in a corresponding time interval.
 */
@Serializable
@SerialName( CarpDataTypes.STEP_COUNT_TYPE_NAME )
data class StepCount( val steps: Int ) : Data
{
    init
    {
        require( steps >= 0 ) { "Number of steps needs to be a positive number." }
    }
}
