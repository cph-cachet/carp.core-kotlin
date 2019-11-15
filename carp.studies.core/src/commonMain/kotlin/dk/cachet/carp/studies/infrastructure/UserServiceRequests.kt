@file:UseSerializers( UUIDSerializer::class )

package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.*
import dk.cachet.carp.common.ddd.*
import dk.cachet.carp.studies.application.UserService
import dk.cachet.carp.studies.domain.users.*
import kotlinx.serialization.*
import kotlin.reflect.KSuspendFunction2


/**
 * Serializable application service requests to [UserService] which can be executed on demand.
 */
@Polymorphic
@Serializable
abstract class UserServiceRequest
{
    companion object
    {
        private val createAccountWithUsername: suspend (UserService, Username) -> Account = UserService::createAccount
        private val createAccountWithEmailAddress: suspend (UserService, EmailAddress) -> Unit = UserService::createAccount
    }


    @Serializable
    data class CreateAccountWithUsername( val username: Username ) :
        UserServiceRequest(),
        ServiceInvoker<UserService, Account> by createServiceInvoker( createAccountWithUsername as KSuspendFunction2<UserService, Username, Account>, "username", username )

    @Serializable
    data class CreateAccountWithEmailAddress( val email: EmailAddress ) :
        UserServiceRequest(),
        ServiceInvoker<UserService, Unit> by createServiceInvoker( createAccountWithEmailAddress as KSuspendFunction2<UserService, EmailAddress, Unit>, "emailAddress", email )

    @Serializable
    data class CreateParticipant( val studyId: UUID, val accountId: UUID ) :
        UserServiceRequest(),
        ServiceInvoker<UserService, Participant> by createServiceInvoker( UserService::createParticipant, studyId, accountId )

    @Serializable
    data class InviteParticipant( val studyId: UUID, val emailAddress: EmailAddress ) :
        UserServiceRequest(),
        ServiceInvoker<UserService, Participant> by createServiceInvoker( UserService::inviteParticipant, studyId, emailAddress )

    @Serializable
    data class GetParticipantsForStudy( val studyId: UUID ) :
        UserServiceRequest(),
        ServiceInvoker<UserService, List<Participant>> by createServiceInvoker( UserService::getParticipantsForStudy, studyId )
}