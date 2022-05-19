package dk.cachet.carp.protocols.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest


val ProtocolFactoryServiceApiMigrator = ApplicationServiceApiMigrator(
    ProtocolFactoryService.API_VERSION,
    ProtocolFactoryServiceRequest.Serializer,
    ProtocolFactoryService.Event.serializer()
)
