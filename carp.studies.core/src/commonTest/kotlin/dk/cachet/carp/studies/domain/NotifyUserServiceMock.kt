package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.*


class NotifyUserServiceMock : NotifyUserService
{
    class AccountConfirmationEmail( val accountId: UUID, val emailAddress: EmailAddress )
    class AccountInvitationEmail( val accountId: UUID, val studyId: UUID, val emailAddress: EmailAddress )


    var lastAccountConfirmationEmail: AccountConfirmationEmail? = null
    var lastAccountInvitationEmail: AccountInvitationEmail? = null

    override fun sendAccountConfirmationEmail( accountId: UUID, emailAddress: EmailAddress )
    {
        lastAccountConfirmationEmail = AccountConfirmationEmail( accountId, emailAddress )
    }

    override fun sendAccountInvitationEmail( accountId: UUID, studyId: UUID, emailAddress: EmailAddress )
    {
        lastAccountInvitationEmail = AccountInvitationEmail( accountId, studyId, emailAddress )
    }

    fun reset()
    {
        lastAccountConfirmationEmail = null
        lastAccountInvitationEmail = null
    }
}