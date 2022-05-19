package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest


val DataStreamServiceApiMigrator = ApplicationServiceApiMigrator(
    DataStreamService.API_VERSION,
    DataStreamServiceRequest.Serializer,
    DataStreamService.Event.serializer()
)
