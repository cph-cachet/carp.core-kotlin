package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.application.ClientManagerSnapshot
import dk.cachet.carp.client.domain.StudyRuntimeSnapshot
import dk.cachet.carp.protocols.infrastructure.JSON


/**
 * Create a [ClientManagerSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun ClientManagerSnapshot.Companion.fromJson( json: String ): ClientManagerSnapshot
{
    return JSON.parse( serializer(), json )
}

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ClientManagerSnapshot.toJson(): String
{
    return JSON.stringify( ClientManagerSnapshot.serializer(), this )
}

/**
 * Create a [StudyRuntimeSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyRuntimeSnapshot.Companion.fromJson( json: String ): StudyRuntimeSnapshot
{
    return JSON.parse( serializer(), json )
}

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyRuntimeSnapshot.toJson(): String
{
    return JSON.stringify( StudyRuntimeSnapshot.serializer(), this )
}