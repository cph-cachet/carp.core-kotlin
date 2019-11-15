package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.studies.domain.StudyDescription
import kotlin.test.*


/**
 * Tests for [StudyDescription] relying on core infrastructure.
 */
class StudyDescriptionTest
{
    @Test
    fun can_serialize_and_deserialize_study_description_using_JSON()
    {
        val description = StudyDescription( "Test" )

        val serialized = description.toJson()
        val parsed = StudyDescription.fromJson( serialized )

        assertEquals( description, parsed )
    }
}