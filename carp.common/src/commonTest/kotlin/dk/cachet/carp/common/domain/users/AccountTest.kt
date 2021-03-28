package dk.cachet.carp.common.domain.users

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [Account].
 */
class AccountTest
{
    @Test
    fun can_serialize_and_deserialize_account_using_JSON()
    {
        val account = Account.withUsernameIdentity( "Test" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( Account.serializer(), account )
        val parsed = json.decodeFromString( Account.serializer(), serialized )

        assertEquals( account, parsed )
    }

    @Test
    fun create_account_withEmailIdentity_succeeds()
    {
        val emailAddress = "test@test.com"
        val account = Account.withEmailIdentity( "test@test.com" )

        val expectedIdentity = EmailAccountIdentity( EmailAddress( emailAddress ))
        assertEquals( expectedIdentity, account.identity )
    }

    @Test
    fun create_account_withUserNameIdentity_succeeds()
    {
        val username = "test"
        val account = Account.withUsernameIdentity( username )

        val expectedIdentity = UsernameAccountIdentity( Username( username ) )
        assertEquals( expectedIdentity, account.identity )
    }

    @Test
    fun hasSameIdentity_succeeds()
    {
        val identity1 = AccountIdentity.fromUsername( "identity1" )
        val account1 = Account( identity = identity1 )
        val account2 = Account( identity = identity1 )

        assertTrue( account1.hasSameIdentity( account2 ) )
    }
}
