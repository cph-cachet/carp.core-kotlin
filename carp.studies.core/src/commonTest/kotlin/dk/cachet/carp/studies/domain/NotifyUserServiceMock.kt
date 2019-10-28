package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.*


class NotifyUserServiceMock : NotifyUserService
{
    class AccountVerificationEmail(val accountId: UUID, val emailAddress: EmailAddress )
    class AccountInvitationEmail( val accountId: UUID, val studyId: UUID, val emailAddress: EmailAddress )


    var lastAccountVerificationEmail: AccountVerificationEmail? = null
    var lastAccountInvitationEmail: AccountInvitationEmail? = null

    override fun sendAccountVerificationEmail( accountId: UUID, emailAddress: EmailAddress )
    {
        lastAccountVerificationEmail = AccountVerificationEmail( accountId, emailAddress )
    }

    override fun sendAccountInvitationEmail( accountId: UUID, studyId: UUID, emailAddress: EmailAddress )
    {
        lastAccountInvitationEmail = AccountInvitationEmail( accountId, studyId, emailAddress )
    }

    fun reset()
    {
        lastAccountVerificationEmail = null
        lastAccountInvitationEmail = null
    }
}