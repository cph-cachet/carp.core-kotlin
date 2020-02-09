package dk.cachet.carp.common.users

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [AccountService].
 */
abstract class AccountServiceTest
{
    val notifyUser: NotifyUserServiceMock = NotifyUserServiceMock()

    @BeforeTest
    fun initializeTest()
    {
        // Initialize/reset mocks which are used in the tests.
        notifyUser.reset()
    }

    /**
     * Create an account service and repository it depends on to be used in the tests.
     *
     * @param notify The user notification service to be used when initializing the account service.
     */
    abstract fun createAccountService( notify: NotifyUserServiceMock = notifyUser ): Pair<AccountService, AccountRepository>


    @Test
    fun createAccount_with_username_succeeds() = runBlockingTest {
        val ( service, repo ) = createAccountService()

        val username = "User"
        val account = service.createAccount( Username( username ) )
        val expectedIdentity = AccountIdentity.fromUsername( username )
        assertEquals( expectedIdentity, account.identity )

        // Verify whether account was added to the repository.
        val foundAccount = repo.findAccountWithIdentity( expectedIdentity )
        assertEquals( account, foundAccount )
    }

    @Test
    fun createAccount_with_existing_username_fails() = runBlockingTest {
        val ( service, _ ) = createAccountService()

        val username = Username( "User" )
        service.createAccount( username )

        assertFailsWith<IllegalArgumentException> {
            service.createAccount( username )
        }
    }

    @Test
    fun createAccount_with_new_email_succeeds() = runBlockingTest {
        val ( service, repo ) = createAccountService()

        val email = EmailAddress( "user@user.com" )
        val expectedIdentity = EmailAccountIdentity( email )
        service.createAccount( email )

        // Verify whether account was added to the repository.
        val foundAccount = repo.findAccountWithIdentity( expectedIdentity )
        assertNotNull( foundAccount )

        // Verify whether user was notified of account creation.
        assertTrue( notifyUser.wasCalled( NotifyUserService::sendAccountVerificationEmail, foundAccount.id, email ) )
    }

    @Test
    fun createAccount_with_existing_email_does_not_notify() = runBlockingTest {
        val ( service, _ ) = createAccountService()

        // Create user, which will get notified, so reset notify mock.
        val emailAddress = EmailAddress( "user@user.com" )
        service.createAccount( emailAddress )
        notifyUser.reset()

        // Create user which already exists, so no notification is sent.
        service.createAccount( emailAddress )
        assertTrue( notifyUser.wasNotCalled( NotifyUserService::sendAccountVerificationEmail ) )
    }
}
