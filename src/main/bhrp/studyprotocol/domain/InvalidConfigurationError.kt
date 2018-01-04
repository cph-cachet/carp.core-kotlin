package bhrp.studyprotocol.domain

import bhrp.studyprotocol.domain.deployment.*


/**
 * Exception which is thrown when performing an operation on [StudyProtocol] would result in an invalid study configuration.
 *
 * Note that this is different from [DeploymentIssue]:
 * intermediate 'work-in-progress' configurations might not be deployable but be valid configurations.
 */
class InvalidConfigurationError( message: String ) : Throwable( message )