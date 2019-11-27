package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


/**
 * Uniquely identifies the person or group that created a [Study].
 */
@Serializable
data class StudyOwner(
    @Serializable( with = UUIDSerializer::class )
    val id: UUID = UUID.randomUUID() )
