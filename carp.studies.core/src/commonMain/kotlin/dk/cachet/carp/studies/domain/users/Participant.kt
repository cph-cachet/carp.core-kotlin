package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies a participant as part of a particular study.
 */
@Serializable
data class Participant(
    @Serializable( with = UUIDSerializer::class )
    val studyId: UUID,
    @Serializable( with = UUIDSerializer::class )
    val id: UUID = UUID.randomUUID() )
