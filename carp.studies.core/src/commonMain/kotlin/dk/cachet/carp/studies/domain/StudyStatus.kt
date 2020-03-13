package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.UUID
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [Study]: the number of participants, progress towards study goal, etc.
 */
@Serializable
sealed class StudyStatus
{
    abstract val studyId: UUID
    /**
     * A descriptive name for the study, as assigned by the [StudyOwner].
     */
    abstract val name: String
    /**
     * The date when this study was created.
     */
    abstract val creationDate: DateTime
    /**
     * Determines whether the study in its current state is ready to be deployed to participants.
     */
    abstract val canDeployToParticipants: Boolean
    /**
     * Determines whether a study protocol can be set/changed for the study.
     */
    abstract val canSetStudyProtocol: Boolean
}

/**
 * Study state for when a study is being configured.
 */
@Serializable
data class ConfiguringStudyStatus(
    override val studyId: UUID,
    override val name: String,
    override val creationDate: DateTime,
    override val canDeployToParticipants: Boolean,
    override val canSetStudyProtocol: Boolean,
    /**
     * Determines whether a study is fully configured and can 'go live'.
     */
    val canGoLive: Boolean
) : StudyStatus()


/**
 * Study state for when a study is 'live'.
 */
@Serializable
data class LiveStudyStatus(
    override val studyId: UUID,
    override val name: String,
    override val creationDate: DateTime,
    override val canDeployToParticipants: Boolean,
    override val canSetStudyProtocol: Boolean
) : StudyStatus()
