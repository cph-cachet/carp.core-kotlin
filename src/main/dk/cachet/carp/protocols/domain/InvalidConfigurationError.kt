package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.deployment.*
import kotlin.reflect.KClass


/**
 * Exception which is thrown when performing an operation on [StudyProtocol] would result in an invalid study configuration.
 *
 * Note that this is different from [DeploymentIssue]:
 * intermediate 'work-in-progress' configurations might not be deployable but be valid configurations.
 */
class InvalidConfigurationError( message: String ) : Throwable( message )


fun<T: Any> notImmutableErrorFor( type: KClass<T> ): InvalidConfigurationError
{
    return InvalidConfigurationError( "Implementations of '${type.simpleName}' should be data classes and may not contain any mutable properties." )
}