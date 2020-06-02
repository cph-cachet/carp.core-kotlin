package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.studies.domain.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import kotlin.test.*


/**
 * Tests for [ParticipantGroupStatus] relying on core infrastructure.
 */
class ParticipantGroupStatusTest
{
    @Test
    fun can_serialize_and_deserialize_ParticipantGroupStatus_using_JSON()
    {
        val studyDeploymentId = UUID.randomUUID()
        val deploymentStatus = StudyDeploymentStatus.Invited( studyDeploymentId, listOf(), null )
        val participants = setOf( DeanonymizedParticipation( UUID.randomUUID(), UUID.randomUUID() ) )
        val groupStatus = ParticipantGroupStatus( deploymentStatus, participants )

        val serialized: String = JSON.stringify( ParticipantGroupStatus.serializer(), groupStatus )
        val parsed: ParticipantGroupStatus = JSON.parse( ParticipantGroupStatus.serializer(), serialized )

        assertEquals( groupStatus, parsed )
    }
}
