package dk.cachet.carp.common.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlin.test.*


class CustomSerializerWrapperTest
{
    @Serializable
    sealed class SealedBase
    {
        @Serializable
        class ExtendsSealed( val answer: Int = 42 ) : SealedBase()
    }

    @Test
    fun can_enforce_serializing_as_polymorph_base_class()
    {
        val wrapper = customSerializerWrapper( SealedBase.ExtendsSealed(), SealedBase.serializer() )
        val json = Json( JsonConfiguration.Stable )

        val serialized = json.stringify( CustomSerializerWrapper.serializer(), wrapper )

        val jsonObject = json.parseJson( serialized ) as JsonObject
        assertTrue( jsonObject.contains( "type" ) )
    }
}
