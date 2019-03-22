package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.*


/**
 * Evaluates and documents potential deployment issues with a [StudyProtocol] configuration.
 */
interface DeploymentIssue
{
    /**
     * A description of the deployment issue.
     */
    val description: String

    /**
     * Determines whether or not this deployment issue is present in the specified [StudyProtocol].
     *
     * @param protocol The [StudyProtocol] to evaluate.
     */
    fun isIssuePresent( protocol: StudyProtocol ): Boolean
}