package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.SensorData
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName( StubDataTypes.STUB_DATA_POINT_TYPE_NAME )
data class StubDataPoint(
    @Required
    val data: String = "Stub",
    override val sensorSpecificData: Data? = null
) : SensorData

@Serializable
@SerialName( StubDataTypes.STUB_DATA_TIME_SPAN_TYPE_NAME )
data class StubDataTimeSpan(
    @Required
    val data: String = "Stub",
    override val sensorSpecificData: Data? = null
) : SensorData
