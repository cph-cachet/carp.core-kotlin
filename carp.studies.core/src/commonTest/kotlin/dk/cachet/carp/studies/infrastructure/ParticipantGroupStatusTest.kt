package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.input.CarpInputDataTypes
import dk.cachet.carp.common.data.input.Sex
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
        val someData = mapOf( CarpInputDataTypes.SEX to Sex.Female )
        val groupStatus = ParticipantGroupStatus( deploymentStatus, participants, someData )

        val serialized: String = JSON.encodeToString( ParticipantGroupStatus.serializer(), groupStatus )
        val parsed: ParticipantGroupStatus = JSON.decodeFromString( ParticipantGroupStatus.serializer(), serialized )

        assertEquals( groupStatus, parsed )
    }
}
