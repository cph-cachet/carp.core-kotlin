package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.data.carp.CARP_NAMESPACE


val STUB_DATA_TYPE: DataType = DataType( CARP_NAMESPACE, "stub" )

class StubDataTypeSamplingScheme : DataTypeSamplingScheme<NoOptionsSamplingConfigurationBuilder>( STUB_DATA_TYPE )
{
    override fun createSamplingConfigurationBuilder(): StubSamplingConfigurationBuilder = StubSamplingConfigurationBuilder
}

typealias StubSamplingConfigurationBuilder = NoOptionsSamplingConfigurationBuilder
