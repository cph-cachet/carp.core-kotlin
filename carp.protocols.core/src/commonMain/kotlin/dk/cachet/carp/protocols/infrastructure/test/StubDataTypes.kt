package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.data.CARP_NAMESPACE
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.data.DataTypeSamplingScheme
import dk.cachet.carp.protocols.domain.data.NoOptionsSamplingConfigurationBuilder


val STUB_DATA_TYPE: DataType = DataType( CARP_NAMESPACE, "stub" )

class StubDataTypeSamplingScheme : DataTypeSamplingScheme<NoOptionsSamplingConfigurationBuilder>( STUB_DATA_TYPE )
{
    override fun createSamplingConfigurationBuilder(): StubSamplingConfigurationBuilder = StubSamplingConfigurationBuilder
}

typealias StubSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
