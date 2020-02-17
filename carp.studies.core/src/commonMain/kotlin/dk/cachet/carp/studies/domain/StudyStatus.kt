package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [Study]: the number of participants, progress towards study goal, etc.
 */
@Serializable
data class StudyStatus(
    val studyId: UUID,
    /**
     * A descriptive name for the study, as assigned by the [StudyOwner].
     */
    val name: String,
    /**
     * The date when this study was created.
     */
    val creationDate: DateTime
)
