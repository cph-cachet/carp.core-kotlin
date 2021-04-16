package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.users.StudyOwner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [StudyOwner] relying on core infrastructure.
 */
class StudyOwnerTest
{
    @Test
    fun can_serialize_and_deserialize_study_owner_using_JSON()
    {
        val owner = StudyOwner()

        val serialized: String = JSON.encodeToString( owner )
        val parsed: StudyOwner = JSON.decodeFromString( serialized )

        assertEquals( owner, parsed )
    }
}
