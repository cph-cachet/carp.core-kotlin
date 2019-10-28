package dk.cachet.carp.studies.application

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.NotifyUserServiceMock
import dk.cachet.carp.studies.domain.users.*
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for implementations of [UserService].
 */
abstract class UserServiceTest
{
    val notifyUser: NotifyUserServiceMock = NotifyUserServiceMock()

    @BeforeTest
    fun initializeTest()
    {
        // Initialize/reset mocks which are used in the tests.
        notifyUser.reset()
    }

    /**
     * Create a user service and repository it depends on to be used in the tests.
     *
     * @param notify The user notification service to be used when initializing the user service.
     */
    abstract fun createUserService( notify: NotifyUserServiceMock = notifyUser ): Pair<UserService, UserRepository>


    @Test
    fun createAccount_with_username_succeeds() = runBlockingTest {
        val ( service, repo ) = createUserService()

        val username = "User"
        val account = service.createAccount( Username( username ) )
        val expectedIdentity = AccountIdentity.fromUsername( username )
        assertEquals( expectedIdentity, account.identities.single() )
        assertEquals( 0, account.studyParticipations.count() )

        // Verify whether account was added to the repository.
        val foundAccount = repo.findAccountWithIdentity( expectedIdentity )
        assertEquals( account, foundAccount )
    }

    @Test
    fun createAccount_with_existing_username_fails() = runBlockingTest {
        val ( service, _ ) = createUserService()

        val username = Username( "User" )
        service.createAccount( username )

        assertFailsWith<IllegalArgumentException> {
            service.createAccount( username )
        }
    }

    @Test
    fun createAccount_with_new_email_succeeds() = runBlockingTest {
        val ( service, repo ) = createUserService()

        val email = EmailAddress( "user@user.com" )
        val expectedIdentity = EmailAccountIdentity( email )
        service.createAccount( email )

        // Verify whether user was notified of account creation.
        assertEquals( email, notifyUser.lastAccountVerificationEmail?.emailAddress )
        val accountId = notifyUser.lastAccountVerificationEmail!!.accountId

        // Verify whether account was added to the repository.
        val foundAccount = repo.findAccountWithIdentity( expectedIdentity )
        assertNotNull( foundAccount )
        assertEquals( accountId, foundAccount.id )
    }

    @Test
    fun createAccount_with_existing_email_does_not_notify() = runBlockingTest {
        val ( service, _ ) = createUserService()

        // Create user, which will get notified, so reset notify mock.
        val emailAddress = EmailAddress( "user@user.com" )
        service.createAccount( emailAddress )
        notifyUser.reset()

        // Create user which already exists, so no notification is sent.
        service.createAccount( emailAddress )
        assertNull( notifyUser.lastAccountVerificationEmail )
    }

    @Test
    fun createParticipant_succeeds() = runBlockingTest {
        val ( service, _ ) = createUserService()

        val account = service.createAccount( Username( "test" ) )
        val studyId = UUID.randomUUID()
        val participant = service.createParticipant( studyId, account.id )

        assertEquals( studyId, participant.studyId )
    }

    @Test
    fun createParticipant_fails_for_unknown_accountid() = runBlockingTest {
        val ( service, _ ) = createUserService()

        val studyId = UUID.randomUUID()
        val unknownAccountId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            service.createParticipant( studyId, unknownAccountId )
        }
    }

    @Test
    fun inviteParticipant_has_matching_studyId() = runBlockingTest {
        val ( service, _ ) = createUserService()
        val studyId = UUID.randomUUID()

        val participant = service.inviteParticipant( studyId, EmailAddress( "test@test.com" ) )

        assertEquals( studyId, participant.studyId )
    }

    @Test
    fun inviteParticipant_creates_new_account_for_new_email() = runBlockingTest {
        val ( service, repo ) = createUserService()

        val studyId = UUID.randomUUID()
        val email = EmailAddress( "test@test.com" )
        service.inviteParticipant( studyId, email )

        // Verify whether user was notified to register account.
        assertEquals( email, notifyUser.lastAccountInvitationEmail?.emailAddress )
        assertEquals( studyId, notifyUser.lastAccountInvitationEmail?.studyId )
        val accountId = notifyUser.lastAccountInvitationEmail!!.accountId

        // Verify whether account was added to the repository.
        val foundAccount = repo.findAccountWithIdentity( EmailAccountIdentity( email ) )
        assertNotNull( foundAccount )
        assertEquals( accountId, foundAccount.id )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun inviteParticipant_with_same_studyId_and_email() = runBlockingTest {
        val ( service, _ ) = createUserService()
        val studyId = UUID.randomUUID()
        val email = EmailAddress( "test@test.com" )

        // The first invite will notify the user to register, so reset notify mock.
        val p1: Participant = service.inviteParticipant( studyId, email )
        notifyUser.reset()

        // Subsequent invitations will return the same participant, and will not notify the user again.
        val p2: Participant = service.inviteParticipant( studyId, email )
        assertTrue( p1.id == p2.id )
        assertNull( notifyUser.lastAccountInvitationEmail )
    }
}