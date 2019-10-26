package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.studies.domain.users.Account


/**
 * A domain service which notifies users about events stemming from application service calls in the study subsystem.
 */
interface NotifyUserService
{
    /**
     * Send an email to [emailAddress] requesting the user to confirm the creation of the specified [account].
     */
    fun sendAccountConfirmationEmail( account: Account, emailAddress: EmailAddress )

    /**
     * Send an email to [emailAddress] inviting the user to participate in the study identified by [studyId],
     * requiring the user to confirm the creation of the specified [account].
     */
    fun sendAccountInvitationEmail( account: Account, studyId: UUID, emailAddress: EmailAddress )
}