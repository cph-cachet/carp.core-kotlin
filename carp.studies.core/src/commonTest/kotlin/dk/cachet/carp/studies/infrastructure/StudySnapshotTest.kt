package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.createComplexStudy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

        val serialized: String = JSON.encodeToString( snapshot )
        val parsed: StudySnapshot = JSON.decodeFromString( serialized )

        assertEquals( snapshot, parsed )
    }
}
