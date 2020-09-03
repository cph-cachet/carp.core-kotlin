package dk.cachet.carp.protocols.domain

import kotlin.reflect.KClass


/**
 * Exception which is thrown when performing an operation on [StudyProtocol] which would result in an invalid study configuration.
 *
 * Note that this is different from [DeploymentIssue]:
 * intermediate 'work-in-progress' configurations might not be deployable but be valid configurations.
 */
@Suppress( "Immutable", "DataClass" )
class InvalidConfigurationError( message: String ) : Throwable( message )


fun <T : Any> notImmutableErrorFor( type: KClass<T> ): InvalidConfigurationError
{
    return InvalidConfigurationError( "Implementations of '${type.simpleName}' should be data classes and may not contain any mutable properties." )
}
