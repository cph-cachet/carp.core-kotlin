package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.studies.domain.Study
import kotlinx.serialization.Serializable


/**
 * A person to be invited or participating in a [Study].
 */
@Serializable
data class Participant( val accountIdentity: AccountIdentity, val id: UUID = UUID.randomUUID() )
