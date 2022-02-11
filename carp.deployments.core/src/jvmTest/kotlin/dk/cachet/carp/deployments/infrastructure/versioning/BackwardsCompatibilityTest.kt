package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.BackwardsCompatibilityTest
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHostTest
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHostTest
import kotlinx.serialization.ExperimentalSerializationApi


@ExperimentalSerializationApi
class DeploymentServiceBackwardsCompatibilityTest :
    BackwardsCompatibilityTest<DeploymentService>( DeploymentService::class )
{
    override fun createService() = DeploymentServiceHostTest.createService()
}


@ExperimentalSerializationApi
class ParticipationServiceBackwardsCompatibilityTest :
    BackwardsCompatibilityTest<ParticipationService>( ParticipationService::class )
{
    override fun createService() = ParticipationServiceHostTest.createService()
        .let { Pair( it.participationService, it.eventBus ) }
}
