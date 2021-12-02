package dk.cachet.carp.clients.application.study

import dk.cachet.carp.common.application.UUID


/**
 * Uniquely identifies a [Study] added to a [ClientManager].
 */
data class StudyId(
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,

    /**
     * The role name of the device in the deployment this study runtime participates in.
     */
    val deviceRoleName: String
)
