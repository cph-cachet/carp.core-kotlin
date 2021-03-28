package dk.cachet.carp.common.application.users

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [Username] relying on core infrastructure.
 */
class UsernameTest
{
    @Test
    fun can_serialize_and_deserialize_username_using_JSON()
    {
        val username = Username( "Test" )

        val json = createDefaultJSON()
        val serialized: String = json.encodeToString( Username.serializer(), username )
        val parsed: Username = json.decodeFromString( Username.serializer(), serialized )

        assertEquals( username, parsed )
    }
}
