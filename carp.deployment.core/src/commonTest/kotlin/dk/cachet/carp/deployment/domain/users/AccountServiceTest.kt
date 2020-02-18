package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [AccountService].
 */
abstract class AccountServiceTest
{
    /**
     * Create an account service to be used in the tests.
     */
    abstract fun createService(): AccountService


    @Test
    fun inviteNewAccount_with_username_succeeds() =
        inviteNewAccountTest( AccountIdentity.fromUsername( "User" ) )

    @Test
    fun inviteNewAccount_with_email_succeeds() =
        inviteNewAccountTest( AccountIdentity.fromEmailAddress( "user@user.com" ) )

    private fun inviteNewAccountTest( identity: AccountIdentity ) = runBlockingTest {
        val service = createService()

        // Create and verify account.
        val participation = Participation( UUID.randomUUID() )
        val account = service.inviteNewAccount( identity, StudyInvitation.empty(), participation )
        assertEquals( identity, account.identity )

        // Verify whether account can be retrieved.
        val foundAccount = service.findAccount( identity )
        assertEquals( foundAccount, account )
    }

    @Test
    fun inviteNewAccount_with_existing_username_fails() =
        inviteNewAccountWithExistingTest( AccountIdentity.fromUsername( "User" ) )

    @Test
    fun inviteNewAccount_with_existing_email_fails() =
        inviteNewAccountWithExistingTest( AccountIdentity.fromEmailAddress( "user@user.com" ) )

    private fun inviteNewAccountWithExistingTest( identity: AccountIdentity ) = runBlockingTest {
        val service = createService()
        val participation = Participation( UUID.randomUUID() )
        service.inviteNewAccount( identity, StudyInvitation.empty(), participation )

        assertFailsWith<IllegalArgumentException> {
            service.inviteNewAccount( identity, StudyInvitation.empty(), participation )
        }
    }

    @Test
    fun inviteExistingAccount_with_new_username_fails() =
        inviteExistingAccountWithNewTest( AccountIdentity.fromUsername( "User" ) )

    @Test
    fun inviteExistingAccount_with_new_email_fails() =
        inviteExistingAccountWithNewTest( AccountIdentity.fromEmailAddress( "user@user.com" ) )

    private fun inviteExistingAccountWithNewTest( identity: AccountIdentity ) = runBlockingTest {
        val service = createService()
        val participation = Participation( UUID.randomUUID() )

        assertFailsWith<IllegalArgumentException> {
            service.inviteExistingAccount( identity, StudyInvitation.empty(), participation )
        }
    }

    @Test
    fun findAccount_null_when_not_found() = runBlockingTest {
        val service = createService()

        val unknownIdentity = AccountIdentity.fromUsername( "Unknown" )
        val foundAccount = service.findAccount( unknownIdentity )
        assertNull( foundAccount )
    }
}
