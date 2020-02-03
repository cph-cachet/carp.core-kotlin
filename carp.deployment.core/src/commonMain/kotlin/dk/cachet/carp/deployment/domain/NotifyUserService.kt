package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID


/**
 * A domain service which notifies users about events stemming from application service calls in the study subsystem.
 */
interface NotifyUserService
{
    /**
     * Send an email to [emailAddress] requesting the user to verify the creation of the account with the specified [accountId].
     */
    fun sendAccountVerificationEmail( accountId: UUID, emailAddress: EmailAddress )

    /**
     * Send an email to [emailAddress] inviting the user to participate in the study identified by [studyId],
     * requiring the user to confirm the creation of the account with the specified [accountId].
     */
    fun sendAccountInvitationEmail( accountId: UUID, studyId: UUID, emailAddress: EmailAddress )
}
