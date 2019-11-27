package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * A [DeploymentIssue] which blocks the deployment of a [StudyProtocol].
 */
interface DeploymentError : DeploymentIssue
