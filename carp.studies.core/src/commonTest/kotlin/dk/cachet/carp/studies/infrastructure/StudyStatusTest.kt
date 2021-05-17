package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.StudyStatus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

        val serialized = JSON.encodeToString<StudyStatus>( status )
        val parsed: StudyStatus = JSON.decodeFromString( serialized )

        assertEquals( status, parsed )
    }
}
