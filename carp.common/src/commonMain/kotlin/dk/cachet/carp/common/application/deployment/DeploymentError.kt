package dk.cachet.carp.common.application.deployment

import dk.cachet.carp.common.domain.StudyProtocol


/**
 * A [DeploymentIssue] which blocks the deployment of a [StudyProtocol].
 */
interface DeploymentError : DeploymentIssue
