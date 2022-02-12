package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest


val RecruitmentServiceApiMigrator = ApplicationServiceApiMigrator(
    RecruitmentService.API_VERSION,
    RecruitmentServiceRequest.Serializer
)
