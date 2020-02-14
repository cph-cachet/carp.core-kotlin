package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.createComplexStudy
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests for [StudySnapshot] relying on core infrastructure.
 */
class StudySnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        val study: Study = createComplexStudy()
        val snapshot: StudySnapshot = study.getSnapshot()

        val serialized: String = snapshot.toJson()
        val parsed: StudySnapshot = StudySnapshot.fromJson( serialized )

        assertEquals( snapshot, parsed )
    }
}
