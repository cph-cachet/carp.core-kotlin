package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.test.infrastructure.SnapshotTest
import dk.cachet.carp.deployments.domain.createComplexParticipantGroup
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot


/**
 * Tests for [ParticipantGroupSnapshot] relying on core infrastructure.
 */
class ParticipantGroupSnapshotTest :
    SnapshotTest<ParticipantGroup, ParticipantGroupSnapshot>( ParticipantGroupSnapshot.serializer() )
{
    override fun createObject() = createComplexParticipantGroup()
    override fun changeSnapshotVersion( toChange: ParticipantGroupSnapshot, version: Int ) =
        toChange.copy( version = version )
}
