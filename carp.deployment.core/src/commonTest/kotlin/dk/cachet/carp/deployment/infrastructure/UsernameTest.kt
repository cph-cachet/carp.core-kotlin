package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.users.Username
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

        val serialized: String = username.toJson()
        val parsed: Username = Username.fromJson( serialized )

        assertEquals( username, parsed )
    }
}
