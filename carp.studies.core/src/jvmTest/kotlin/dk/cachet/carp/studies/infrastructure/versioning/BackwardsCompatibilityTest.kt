package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.BackwardsCompatibilityTest
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHostTest
import kotlinx.serialization.ExperimentalSerializationApi


@ExperimentalSerializationApi
class StudyServiceBackwardsCompatibilityTest :
    BackwardsCompatibilityTest<StudyService>( StudyService::class )
{
    override fun createService() = StudyServiceHostTest.createService()
}


@ExperimentalSerializationApi
class RecruitmentServiceBackwardsCompatibilityTest :
    BackwardsCompatibilityTest<RecruitmentService>( RecruitmentService::class )
{
    override fun createService() = RecruitmentServiceHostTest.createSUT()
        .let { Pair( it.recruitmentService, it.eventBus ) }
}
