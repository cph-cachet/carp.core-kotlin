package dk.cachet.carp.common.application.deployment

import dk.cachet.carp.common.domain.StudyProtocol


/**
 * A [DeploymentIssue] which indicates a potential error in a [StudyProtocol].
 */
interface DeploymentWarning : DeploymentIssue
