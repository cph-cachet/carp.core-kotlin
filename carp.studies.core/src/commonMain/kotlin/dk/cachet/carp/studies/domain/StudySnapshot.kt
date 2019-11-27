package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


@Serializable
data class StudySnapshot(
    val studyId: UUID,
    val ownerId: UUID,
    val name: String,
    val description: StudyDescription,
    val participantIds: List<UUID> )
{
    companion object
    {
        /**
         * Create a snapshot of the specified [Study].
         *
         * @param study The [Study] to create a snapshot for.
         */
        fun fromStudy( study: Study ): StudySnapshot
        {
            return StudySnapshot(
                studyId = study.id,
                ownerId = study.owner.id,
                name = study.name,
                description = study.description,
                participantIds = study.participantIds.toList() )
        }
    }
}
