package dk.cachet.carp.common.data

import dk.cachet.carp.common.serialization.CLASS_DISCRIMINATOR
import dk.cachet.carp.common.serialization.createDefaultJSON
import kotlinx.serialization.PolymorphicSerializer
import kotlin.test.*


/**
 * Tests for serializing [Data] types in [dk.cachet.carp.common.data].
 */
class DataSerializationTest
{
    @Test
    fun can_serialize_data_nonpolymorphically()
    {
        val json = createDefaultJSON()
        val data = FreeFormText( "some text" )
        val serializer = FreeFormText.serializer()

        val serialized = json.encodeToString( serializer, data )
        assertEquals( """{"text":"some text"}""", serialized )
    }

    @Test
    fun class_discriminator_of_serialized_data_equals_matching_data_type()
    {
        val json = createDefaultJSON()
        val data = FreeFormText( "some text" )
        val serializer = PolymorphicSerializer( Data::class )

        val serialized = json.encodeToString( serializer, data )
        assertEquals(
            "{\"$CLASS_DISCRIMINATOR\":\"${CarpDataTypes.FREE_FORM_TEXT_TYPE_NAME}\",\"text\":\"some text\"}",
            serialized
        )
    }
}
