package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.deployment.domain.createComplexParticipantGroup
import dk.cachet.carp.deployment.domain.users.ParticipantGroup
import dk.cachet.carp.deployment.domain.users.ParticipantGroupSnapshot
import kotlin.test.*


/**
 * Tests for [ParticipantGroupSnapshot] relying on core infrastructure.
 */
class ParticipantGroupSnapshotTest
{
    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        JSON = createDeploymentSerializer( STUBS_SERIAL_MODULE )

        val group: ParticipantGroup = createComplexParticipantGroup()
        val snapshot: ParticipantGroupSnapshot = group.getSnapshot()

        val serialized: String = snapshot.toJson()
        val parsed: ParticipantGroupSnapshot = ParticipantGroupSnapshot.fromJson( serialized )

        assertEquals( snapshot, parsed )
    }
}
