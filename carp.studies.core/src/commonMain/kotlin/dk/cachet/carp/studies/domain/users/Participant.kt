package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.studies.domain.Study
import kotlinx.serialization.Serializable


/**
 * A person to be invited or participating in a [Study].
 */
@Serializable
data class Participant( val accountIdentity: AccountIdentity, val id: UUID = UUID.randomUUID() )
