package dk.cachet.carp.clients.infrastructure

import dk.cachet.carp.clients.domain.StudyRuntime
import dk.cachet.carp.clients.domain.StudyRuntimeSnapshot
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [StudyRuntimeSnapshot].
 */
class StudyRuntimeSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON() = runSuspendTest {
        val runtime = StudyRuntime( UUID.randomUUID(), "Some device" )
        val snapshot = runtime.getSnapshot()

        val serialized = JSON.encodeToString( snapshot )
        val parsed: StudyRuntimeSnapshot = JSON.decodeFromString( serialized )
        assertEquals( snapshot, parsed )
    }
}
