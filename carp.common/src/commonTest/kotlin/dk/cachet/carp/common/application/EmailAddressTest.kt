package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [EmailAddress].
 */
class EmailAddressTest
{
    @Test
    fun can_serialize_and_deserialize_email_address_using_JSON()
    {
        val email = EmailAddress( "test@test.com" )

        val json = createDefaultJSON()
        val serialized: String = json.encodeToString( EmailAddress.serializer(), email )
        val parsed: EmailAddress = json.decodeFromString( EmailAddress.serializer(), serialized )

        assertEquals( email, parsed )
    }
}
