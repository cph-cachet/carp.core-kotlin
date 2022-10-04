package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.test.infrastructure.SnapshotTest
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.createComplexStudy


/**
 * Tests for [StudySnapshot] relying on core infrastructure.
 */
class StudySnapshotTest : SnapshotTest<Study, StudySnapshot>( StudySnapshot.serializer() )
{
    override fun createObject() = createComplexStudy()
    override fun changeSnapshotVersion( toChange: StudySnapshot, version: Int ) = toChange.copy( version = version )
}
