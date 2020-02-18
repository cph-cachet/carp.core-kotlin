package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies the person or group that created a [Study].
 */
@Serializable
data class StudyOwner( val id: UUID = UUID.randomUUID() )
