@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.versioning.BackwardsCompatibilityTest
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import kotlinx.serialization.ExperimentalSerializationApi


@ExperimentalSerializationApi
class DataStreamServiceBackwardsCompatibilityTest :
    BackwardsCompatibilityTest<DataStreamService>( DataStreamService::class )
{
    override fun createService() = Pair( InMemoryDataStreamService(), SingleThreadedEventBus() )
}
