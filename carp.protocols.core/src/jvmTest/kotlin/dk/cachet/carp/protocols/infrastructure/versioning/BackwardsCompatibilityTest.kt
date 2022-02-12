package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.versioning.BackwardsCompatibilityTest
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHostTest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHostTest
import kotlinx.serialization.ExperimentalSerializationApi


@ExperimentalSerializationApi
class ProtocolServiceBackwardsCompatibilityTest :
    BackwardsCompatibilityTest<ProtocolService>( ProtocolService::class )
{
    override fun createService() = Pair( ProtocolServiceHostTest.createService(), SingleThreadedEventBus() )
}


@ExperimentalSerializationApi
class ProtocolFactoryServiceBackwardsCompatibilityTest :
    BackwardsCompatibilityTest<ProtocolFactoryService>( ProtocolFactoryService::class )
{
    override fun createService() = Pair( ProtocolFactoryServiceHostTest.createService(), SingleThreadedEventBus() )
}
