package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.test.infrastructure.SnapshotTest
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.createComplexRecruitment
import dk.cachet.carp.studies.domain.users.Recruitment
import dk.cachet.carp.studies.domain.users.RecruitmentSnapshot


/**
 * Tests for [StudySnapshot] relying on core infrastructure.
 */
class RecruitmentSnapshotTest : SnapshotTest<Recruitment, RecruitmentSnapshot>( RecruitmentSnapshot.serializer() )
{
    override fun createObject() = createComplexRecruitment()
    override fun changeSnapshotVersion( toChange: RecruitmentSnapshot, version: Int ) =
        toChange.copy( version = version )
}
