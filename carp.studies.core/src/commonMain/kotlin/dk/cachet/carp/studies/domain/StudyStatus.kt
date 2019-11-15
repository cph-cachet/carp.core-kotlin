package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [Study]: the number of participants, progress towards study goal, etc.
 */
@Serializable
data class StudyStatus(
    @Serializable( with = UUIDSerializer::class )
    val studyId: UUID,
    /**
     * A descriptive name for the study, as assigned by the [StudyOwner].
     */
    val name: String )