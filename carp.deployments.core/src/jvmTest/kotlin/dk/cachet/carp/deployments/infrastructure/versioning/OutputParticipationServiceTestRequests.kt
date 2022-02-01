package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHostTest
import dk.cachet.carp.deployments.application.ParticipationServiceTest
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceLog


private val services = ParticipationServiceHostTest.createService()

class OutputParticipationServiceTestRequests :
    OutputTestRequests<ParticipationService>( ParticipationServiceLog( services.first ) ),
        ParticipationServiceTest
{
    override fun createService(): Triple<ParticipationService, DeploymentService, AccountService> =
        Triple( loggedService, services.second, services.third )
}
