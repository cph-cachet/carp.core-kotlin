package dk.cachet.carp.clients.infrastructure

import dk.cachet.carp.clients.domain.study.Study
import dk.cachet.carp.clients.domain.study.StudySnapshot
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.test.infrastructure.SnapshotTest


/**
 * Tests for [StudySnapshot].
 */
class StudySnapshotTest : SnapshotTest<Study, StudySnapshot>( StudySnapshot.serializer() )
{
    override fun createObject() = Study( UUID.randomUUID(), "Some device" )
    override fun changeSnapshotVersion( toChange: StudySnapshot, version: Int ) = toChange.copy( version = version )
}
