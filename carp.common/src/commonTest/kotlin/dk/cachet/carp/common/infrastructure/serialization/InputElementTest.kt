package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.data.input.elements.Text
import kotlinx.serialization.*
import kotlinx.serialization.builtins.SetSerializer
import kotlin.test.*


/**
 * Tests for [InputElement] relying on core infrastructure.
 */
class InputElementTest
{
    @Test
    fun can_serialize_and_deserialize_InputElement_polymorphic()
    {
        val element = Text( "How do you feel?" )

        val json = createDefaultJSON()
        val serializer = PolymorphicSerializer( InputElement::class )
        val serialized = json.encodeToString( serializer, element )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( element, parsed )
    }

    @Test
    fun can_serialize_set_of_InputElement_polymorphic()
    {
        val elements: Set<InputElement<*>> = setOf( Text( "How do you feel?" ) )

        val json = createDefaultJSON()
        val serializer = SetSerializer( PolymorphicSerializer( InputElement::class ) )
        val serialized = json.encodeToString( serializer, elements )
        val parsed = json.decodeFromString( serializer, serialized )

        assertEquals( elements, parsed )
    }
}
