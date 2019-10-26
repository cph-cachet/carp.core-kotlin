package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.EmailAddress
import kotlin.test.*


/**
 * Tests for [Account].
 */
class AccountTest
{
    @Test
    fun create_account_withEmailIdentity_succeeds()
    {
        val emailAddress = "test@test.com"
        val account = Account.withEmailIdentity( "test@test.com" )

        val expectedIdentity = EmailAccountIdentity( EmailAddress( emailAddress ))
        assertEquals( expectedIdentity, account.identities.single() )
    }

    @Test
    fun create_account_withUserNameIdentity_succeeds()
    {
        val username = "test"
        val account = Account.withUsernameIdentity( username )

        val expectedIdentity = UsernameAccountIdentity( Username( username ) )
        assertEquals( expectedIdentity, account.identities.single() )
    }

    @Test
    fun hasMatchingIdentity_succeeds()
    {
        val identity1 = AccountIdentity.fromUsername( "identity1" )
        val identity2 = AccountIdentity.fromUsername( "identity2" )
        val account1 = Account( identities = listOf( identity1 ) )
        val account2 = Account( identities = listOf( identity1, identity2 ) )

        assertTrue( account1.hasMatchingIdentity( account2 ) )
    }
}