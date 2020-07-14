package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlin.test.*


/**
 * Tests for [StudyInvitation] relying on core infrastructure.
 */
class StudyInvitationTest
{
    @Test
    fun can_serialize_and_deserialize_study_invitation_using_JSON()
    {
        val applicationData = """{"extraData":"42"}"""
        val invitation = StudyInvitation( "Test", "Description", applicationData )

        val serialized = invitation.toJson()
        val parsed = StudyInvitation.fromJson( serialized )

        assertEquals( invitation, parsed )
    }

    @Test
    fun can_deserialize_study_invitation_without_applicationData()
    {
        val serialized = """{"name":"Test","description":"Description"}"""

        StudyInvitation.fromJson( serialized )
    }
}
