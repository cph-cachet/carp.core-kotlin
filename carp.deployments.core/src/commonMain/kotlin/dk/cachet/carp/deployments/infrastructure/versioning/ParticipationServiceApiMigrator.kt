package dk.cachet.carp.deployments.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceInvoker
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest


val ParticipationServiceApiMigrator = ApplicationServiceApiMigrator(
    ParticipationService.API_VERSION,
    ParticipationServiceInvoker,
    ParticipationServiceRequest.Serializer,
    ParticipationService.Event.serializer()
)
