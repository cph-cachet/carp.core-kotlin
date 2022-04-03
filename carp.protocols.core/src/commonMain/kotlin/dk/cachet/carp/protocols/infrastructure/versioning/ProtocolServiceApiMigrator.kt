package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest


val ProtocolServiceApiMigrator = ApplicationServiceApiMigrator(
    ProtocolService.API_VERSION,
    ProtocolServiceRequest.Serializer,
    ProtocolService.Event.serializer()
)
