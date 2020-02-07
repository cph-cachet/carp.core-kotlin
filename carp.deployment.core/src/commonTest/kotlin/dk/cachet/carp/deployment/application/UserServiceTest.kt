package dk.cachet.carp.deployment.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.common.users.Username
import dk.cachet.carp.deployment.domain.NotifyUserService
import dk.cachet.carp.deployment.domain.NotifyUserServiceMock
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.UserRepository
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
        assertEquals( expectedIdentity, account.identity )

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

        // Verify whether account was added to the repository.
        val foundAccount = repo.findAccountWithIdentity( expectedIdentity )
        assertNotNull( foundAccount )

        // Verify whether user was notified of account creation.
        assertTrue( notifyUser.wasCalled( NotifyUserService::sendAccountVerificationEmail, foundAccount.id, email ) )
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
        assertTrue( notifyUser.wasNotCalled( NotifyUserService::sendAccountVerificationEmail ) )
    }

    @Test
    fun addParticipation_has_matching_studyDeploymentId() = runBlockingTest {
        val ( service, _ ) = createUserService()

        val account = service.createAccount( Username( "test" ) )
        val studyDeploymentId = UUID.randomUUID()
        val participation = service.addParticipation( studyDeploymentId, account.identity )

        assertEquals( studyDeploymentId, participation.studyDeploymentId )
    }

    @Test
    fun addParticipation_creates_new_account_for_new_identity() = runBlockingTest {
        val ( service, repo ) = createUserService()

        val studyDeploymentId = UUID.randomUUID()
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )
        service.addParticipation( studyDeploymentId, emailIdentity )

        // Verify whether account was added to the repository.
        val foundAccount = repo.findAccountWithIdentity( emailIdentity )
        assertNotNull( foundAccount )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipation_with_same_studyDeploymentId_and_identity() = runBlockingTest {
        val ( service, _ ) = createUserService()
        val studyDeploymentId = UUID.randomUUID()
        val emailIdentity = AccountIdentity.fromEmailAddress( "test@test.com" )

        // Adding the same identity to a deployment returns the same participation.
        val p1: Participation = service.addParticipation( studyDeploymentId, emailIdentity )
        val p2: Participation = service.addParticipation( studyDeploymentId, emailIdentity )
        assertTrue( p1.id == p2.id )
    }

    @Test
    fun getParticipantsForStudy_succeeds() = runBlockingTest {
        val ( service, _ ) = createUserService()
        val account = service.createAccount( Username( "test" ) )
        val studyDeploymentId = UUID.randomUUID()
        val participation = service.addParticipation( studyDeploymentId, account.identity )

        val participations = service.getParticipationsForStudyDeployment( studyDeploymentId )

        assertEquals( participation, participations.single() )
    }
}
