package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataTimeType
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.DataTypeMetaDataMap
import dk.cachet.carp.common.application.sampling.DataTypeSamplingScheme
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfiguration
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfigurationBuilder
import dk.cachet.carp.common.application.sampling.SamplingConfiguration


/**
 * Stub data types for unit tests.
 */
object StubDataTypes : DataTypeMetaDataMap()
{
    internal const val STUB_DATA_POINT_TYPE_NAME = "${CarpDataTypes.CARP_NAMESPACE}.stubpoint"
    val STUB_POINT = add( STUB_DATA_POINT_TYPE_NAME, "Stub data point", DataTimeType.POINT )

    internal const val STUB_DATA_TIME_SPAN_TYPE_NAME = "${CarpDataTypes.CARP_NAMESPACE}.stubtimespan"
    val STUB_TIME_SPAN = add( STUB_DATA_TIME_SPAN_TYPE_NAME, "Stub data time span", DataTimeType.TIME_SPAN )
}


val STUB_DATA_POINT_TYPE: DataType = DataType.fromString( StubDataTypes.STUB_DATA_POINT_TYPE_NAME )
val STUB_DATA_TIME_SPAN_TYPE: DataType = DataType.fromString( StubDataTypes.STUB_DATA_TIME_SPAN_TYPE_NAME )


class StubDataTypeSamplingScheme :
    DataTypeSamplingScheme<NoOptionsSamplingConfigurationBuilder>( StubDataTypes.STUB_POINT, NoOptionsSamplingConfiguration )
{
    override fun createSamplingConfigurationBuilder(): NoOptionsSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder

    override fun isValid( configuration: SamplingConfiguration ): Boolean = configuration is NoOptionsSamplingConfiguration
}
