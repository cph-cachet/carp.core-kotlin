package dk.cachet.carp.common.users

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
    fun createAccount_with_username_succeeds() =
        createAccountTest( AccountIdentity.fromUsername( "User" ) )

    @Test
    fun createAccount_with_email_succeeds() =
        createAccountTest( AccountIdentity.fromEmailAddress( "user@user.com" ) )

    private fun createAccountTest( identity: AccountIdentity ) = runBlockingTest {
        val service = createService()

        // Create and verify account.
        val account = service.createAccount( identity )
        assertEquals( identity, account.identity )

        // Verify whether account can be retrieved.
        val foundAccount = service.findAccount( identity )
        assertEquals( foundAccount, account )
    }

    @Test
    fun createAccount_with_existing_username_fails() =
        createExistingAccountTest( AccountIdentity.fromUsername( "User" ) )

    @Test
    fun createAccount_with_existing_email_fails() =
        createExistingAccountTest( AccountIdentity.fromEmailAddress( "user@user.com" ) )

    private fun createExistingAccountTest( identity: AccountIdentity ) = runBlockingTest {
        val service = createService()
        service.createAccount( identity )

        assertFailsWith<IllegalArgumentException> {
            service.createAccount( identity )
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
