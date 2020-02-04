package dk.cachet.carp.common.users

import dk.cachet.carp.common.serialization.createDefaultJSON
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
        val serialized: String = json.stringify( Username.serializer(), username )
        val parsed: Username = json.parse( Username.serializer(), serialized )

        assertEquals( username, parsed )
    }
}
