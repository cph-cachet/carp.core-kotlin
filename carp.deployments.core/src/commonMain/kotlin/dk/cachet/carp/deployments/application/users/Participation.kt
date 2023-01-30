package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Uniquely identifies a participation of an account in a study deployment.
 */
@Serializable
@JsExport
data class Participation(
    val studyDeploymentId: UUID,
    @Required
    val assignedRoles: AssignedTo = AssignedTo.All,
    @Required
    val participantId: UUID = UUID.randomUUID(),
)
