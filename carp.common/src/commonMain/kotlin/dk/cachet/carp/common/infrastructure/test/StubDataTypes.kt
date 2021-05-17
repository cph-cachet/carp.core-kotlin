package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.sampling.DataTypeSamplingScheme
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfiguration
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingConfigurationBuilder
import dk.cachet.carp.common.application.sampling.SamplingConfiguration


val STUB_DATA_TYPE: DataType = DataType( CarpDataTypes.CARP_NAMESPACE, "stub" )
val STUB_DATA_TYPE_METADATA: DataTypeMetaData = DataTypeMetaData( STUB_DATA_TYPE )

class StubDataTypeSamplingScheme :
    DataTypeSamplingScheme<NoOptionsSamplingConfigurationBuilder>( STUB_DATA_TYPE_METADATA )
{
    override fun createSamplingConfigurationBuilder(): NoOptionsSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder

    override fun isValid( configuration: SamplingConfiguration ): Boolean = configuration is NoOptionsSamplingConfiguration
}
