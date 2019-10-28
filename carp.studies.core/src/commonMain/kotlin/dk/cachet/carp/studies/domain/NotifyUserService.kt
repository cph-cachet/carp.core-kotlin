package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.*


/**
 * A domain service which notifies users about events stemming from application service calls in the study subsystem.
 */
interface NotifyUserService
{
    /**
     * Send an email to [emailAddress] requesting the user to confirm the creation of the account with the specified [accountId].
     */
    fun sendAccountConfirmationEmail( accountId: UUID, emailAddress: EmailAddress )

    /**
     * Send an email to [emailAddress] inviting the user to participate in the study identified by [studyId],
     * requiring the user to confirm the creation of the account with the specified [accountId].
     */
    fun sendAccountInvitationEmail( accountId: UUID, studyId: UUID, emailAddress: EmailAddress )
}