package dk.cachet.carp.common

import dk.cachet.carp.common.serialization.createDefaultJSON
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
        val serialized: String = json.stringify( EmailAddress.serializer(), email )
        val parsed: EmailAddress = json.parse( EmailAddress.serializer(), serialized )

        assertEquals( email, parsed )
    }
}
