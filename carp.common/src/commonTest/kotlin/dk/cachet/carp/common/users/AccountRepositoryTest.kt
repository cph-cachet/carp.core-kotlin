package dk.cachet.carp.common.users

import dk.cachet.carp.common.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


/**
 * Tests for implementations of [AccountRepository].
 */
interface AccountRepositoryTest
{
    fun createAccountRepository(): AccountRepository


    fun createRepoWithTestAccount(): Pair<AccountRepository, Account>
    {
        val repo = createAccountRepository()
        val testAccount = Account.withUsernameIdentity( "test" )

        repo.addAccount( testAccount )
        return Pair( repo, testAccount )
    }

    @Test
    fun cant_add_account_with_id_that_already_exists()
    {
        val repo = createAccountRepository()
        val id = UUID.randomUUID()
        val username1 = UsernameAccountIdentity( "test" )
        val username2 = UsernameAccountIdentity( "test2" )
        val account1 = Account( username1, id )
        val account2 = Account( username2, id )
        repo.addAccount( account1 )

        assertFailsWith<IllegalArgumentException>
        {
            repo.addAccount( account2 )
        }
    }

    @Test
    fun cant_add_account_with_identity_that_already_exists()
    {
        val repo = createAccountRepository()
        val username = "test"
        val account1 = Account.withUsernameIdentity( username )
        val account2 = Account.withUsernameIdentity( username )
        repo.addAccount( account1 )

        assertFailsWith<IllegalArgumentException>
        {
            repo.addAccount( account2 )
        }
    }

    @Test
    fun findAccountWithId_succeeds()
    {
        val ( repo, account ) = createRepoWithTestAccount()

        val foundAccount = repo.findAccountWithId( account.id )
        assertEquals( account, foundAccount )
    }

    @Test
    fun findAccountWithId_null_when_not_found()
    {
        val repo = createAccountRepository()

        val foundAccount = repo.findAccountWithId( UUID.randomUUID() )
        assertNull( foundAccount )
    }

    @Test
    fun findAccountWithIdentity_succeeds()
    {
        val repo = createAccountRepository()
        val username = "test"
        val account = Account.withUsernameIdentity( username )
        repo.addAccount( account )

        val foundAccount = repo.findAccountWithIdentity( AccountIdentity.fromUsername( username ) )
        assertEquals( account, foundAccount )
    }

    @Test
    fun findAccountWithIdentity_null_when_not_found()
    {
        val ( repo, _ ) = createRepoWithTestAccount()

        val foundAccount = repo.findAccountWithIdentity( AccountIdentity.fromEmailAddress( "non@existing.com" ) )
        assertNull( foundAccount )
    }
}
