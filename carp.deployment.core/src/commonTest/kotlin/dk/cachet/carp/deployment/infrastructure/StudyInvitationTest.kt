package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlin.test.*


/**
 * Tests for [StudyInvitation] relying on core infrastructure.
 */
class StudyInvitationTest
{
    @Test
    fun can_serialize_and_deserialize_study_description_using_JSON()
    {
        val invitation = StudyInvitation( "Test" )

        val serialized = invitation.toJson()
        val parsed = StudyInvitation.fromJson( serialized )

        assertEquals( invitation, parsed )
    }
}
