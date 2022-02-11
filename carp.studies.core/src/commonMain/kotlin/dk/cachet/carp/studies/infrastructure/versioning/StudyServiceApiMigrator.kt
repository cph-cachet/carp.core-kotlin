package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest


val StudyServiceApiMigrator = ApplicationServiceApiMigrator(
    StudyService.API_VERSION,
    StudyServiceRequest.Serializer
)
