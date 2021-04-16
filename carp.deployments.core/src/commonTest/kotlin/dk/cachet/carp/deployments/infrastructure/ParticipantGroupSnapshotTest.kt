package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.deployments.domain.createComplexParticipantGroup
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot
import kotlin.test.*


/**
 * Tests for [ParticipantGroupSnapshot] relying on core infrastructure.
 */
class ParticipantGroupSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        JSON = createTestJSON()

        val group: ParticipantGroup = createComplexParticipantGroup()
        val snapshot: ParticipantGroupSnapshot = group.getSnapshot()

        val serialized: String = snapshot.toJson()
        val parsed: ParticipantGroupSnapshot = ParticipantGroupSnapshot.fromJson( serialized )

        assertEquals( snapshot, parsed )
    }
}
