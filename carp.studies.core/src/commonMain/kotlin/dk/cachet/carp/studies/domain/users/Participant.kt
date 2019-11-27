package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies a participant as part of a particular study.
 */
@Serializable
data class Participant(
    val studyId: UUID,
    val id: UUID = UUID.randomUUID() )
