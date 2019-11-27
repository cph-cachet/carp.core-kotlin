package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.*
import kotlin.test.*


/**
 * Tests for [Account] relying on core infrastructure.
 */
class AccountTest
{
    @Test
    fun can_serialize_and_deserialize_account_using_JSON()
    {
        var account = Account.withUsernameIdentity( "Test" )
        val participation = Participant( UUID.randomUUID() )
        account = account.copy( studyParticipations = setOf( participation ) )

        val serialized = account.toJson()
        val parsed = Account.fromJson( serialized )

        assertEquals( account, parsed )
    }
}
