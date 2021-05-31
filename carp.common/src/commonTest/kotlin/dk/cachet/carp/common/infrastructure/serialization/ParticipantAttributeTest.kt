package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.users.ParticipantAttribute
import kotlin.test.*


/**
 * Tests for [ParticipantAttribute] relying on core infrastructure.
 */
class ParticipantAttributeTest
{
    @Test
    fun can_serialize_and_deserialize_DefaultParticipantAttribute_polymorphic()
    {
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "test" ) )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( ParticipantAttribute.serializer(), attribute )
        val parsed = json.decodeFromString( ParticipantAttribute.serializer(), serialized )

        assertEquals( attribute, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_CustomParticipantAttribute_polymorphic()
    {
        val attribute = ParticipantAttribute.CustomParticipantAttribute( Text( "Favorite movie" ) )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( ParticipantAttribute.serializer(), attribute )
        val parsed = json.decodeFromString( ParticipantAttribute.serializer(), serialized )

        assertEquals( attribute, parsed )
    }
}
