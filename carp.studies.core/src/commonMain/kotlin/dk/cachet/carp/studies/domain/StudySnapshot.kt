package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


@Serializable
data class StudySnapshot(
    @Serializable( with = UUIDSerializer::class )
    val studyId: UUID,
    @Serializable( with = UUIDSerializer::class )
    val ownerId: UUID,
    val name: String )
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
            return StudySnapshot( study.id, study.owner.id, study.name )
        }
    }
}