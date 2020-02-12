package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.studies.domain.StudyOwner
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

        val serialized: String = owner.toJson()
        val parsed: StudyOwner = StudyOwner.fromJson( serialized )

        assertEquals( owner, parsed )
    }
}
