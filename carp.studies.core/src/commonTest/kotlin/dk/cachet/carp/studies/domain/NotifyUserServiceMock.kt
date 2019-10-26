package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.studies.domain.users.Account


class NotifyUserServiceMock : NotifyUserService
{
    class AccountConfirmationEmail( val account: Account, val emailAddress: EmailAddress )
    class AccountInvitationEmail( val account: Account, val studyId: UUID, val emailAddress: EmailAddress )


    var lastAccountConfirmationEmail: AccountConfirmationEmail? = null
    var lastAccountInvitationEmail: AccountInvitationEmail? = null

    override fun sendAccountConfirmationEmail( account: Account, emailAddress: EmailAddress )
    {
        lastAccountConfirmationEmail = AccountConfirmationEmail( account, emailAddress )
    }

    override fun sendAccountInvitationEmail( account: Account, studyId: UUID, emailAddress: EmailAddress )
    {
        lastAccountInvitationEmail = AccountInvitationEmail( account, studyId, emailAddress )
    }

    fun reset()
    {
        lastAccountConfirmationEmail = null
        lastAccountInvitationEmail = null
    }
}