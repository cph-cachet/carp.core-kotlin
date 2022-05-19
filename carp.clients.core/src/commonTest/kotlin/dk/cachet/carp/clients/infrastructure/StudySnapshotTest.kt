package dk.cachet.carp.clients.infrastructure

import dk.cachet.carp.clients.domain.study.Study
import dk.cachet.carp.clients.domain.study.StudySnapshot
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [StudySnapshot].
 */
class StudySnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON() = runTest {
        val study = Study( UUID.randomUUID(), "Some device" )
        val snapshot = study.getSnapshot()

        val serialized = JSON.encodeToString( snapshot )
        val parsed: StudySnapshot = JSON.decodeFromString( serialized )
        assertEquals( snapshot, parsed )
    }
}
