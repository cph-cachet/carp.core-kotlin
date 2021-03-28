package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.domain.StudyStatus
import kotlin.test.*


/**
 * Tests for [StudyStatus] relying on core infrastructure.
 */
class StudyStatusTest
{
    @Test
    fun can_serialize_and_deserialize_study_status_using_JSON()
    {
        val status = StudyStatus.Configuring(
            UUID.randomUUID(), "Test", DateTime.now(),
            canSetInvitation = false,
            canSetStudyProtocol = true,
            canDeployToParticipants = false,
            canGoLive = false )

        val serialized = status.toJson()
        val parsed = StudyStatus.fromJson( serialized )

        assertEquals( status, parsed )
    }
}
