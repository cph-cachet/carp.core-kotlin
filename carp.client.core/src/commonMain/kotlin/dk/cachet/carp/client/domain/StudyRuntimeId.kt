package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID


/**
 * Uniquely identifies a [StudyRuntime] running on a [ClientManager].
 */
data class StudyRuntimeId(
    /**
     * The ID of the deployed study for which to collect data.
     */
    val studyDeploymentId: UUID,

    /**
     * The role name of the device in the deployment this study runtime participates in.
     */
    val deviceRoleName: String
)
