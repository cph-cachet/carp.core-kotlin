package dk.cachet.carp.common.users

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.test.Mock


class NotifyUserServiceMock : Mock<NotifyUserService>(), NotifyUserService
{
    override fun sendAccountVerificationEmail( accountId: UUID, emailAddress: EmailAddress ) =
        trackCall( NotifyUserService::sendAccountVerificationEmail, accountId, emailAddress )

    override fun sendAccountInvitationEmail( accountId: UUID, studyId: UUID, emailAddress: EmailAddress ) =
        trackCall( NotifyUserService::sendAccountInvitationEmail, accountId, studyId, emailAddress )
}
