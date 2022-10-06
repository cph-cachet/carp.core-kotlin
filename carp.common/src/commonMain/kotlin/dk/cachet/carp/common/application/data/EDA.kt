package dk.cachet.carp.common.application.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Holds single-channel electrodermal activity (EDA) data, represented as skin conductance.
 * Among others, also known as galvanic skin response (GSR) and skin conductance response/level.
 */
@Serializable
@SerialName( CarpDataTypes.EDA_TYPE_NAME )
data class EDA( val microSiemens: Double ) : Data
{
    init
    {
        require( microSiemens >= 0 ) { "EDA conductance in microsiemens needs to be a positive value." }
    }
}
