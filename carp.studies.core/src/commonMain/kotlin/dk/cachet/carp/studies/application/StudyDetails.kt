package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.StudyOwner
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * Contains detailed information about a [Study], such as the configured study protocol.
 */
@Serializable
data class StudyDetails(
    val studyId: UUID,
    /**
     * The person or group that created this [Study].
     */
    val studyOwner: StudyOwner,
    /**
     * A descriptive name for the study, assigned by, and only visible to, the [StudyOwner].
     */
    val name: String,
    /**
     * The date when this study was created.
     */
    val creationDate: Instant,
    /**
     * A description for the study, assigned by, and only visible to, the [StudyOwner].
     */
    val description: String,
    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    val invitation: StudyInvitation,
    /**
     * A snapshot of the protocol to use in this study, or null when not yet defined.
     */
    val protocolSnapshot: StudyProtocolSnapshot?
)
