package dk.cachet.carp.client.infrastructure

import dk.cachet.carp.client.domain.StudyRuntimeSnapshot
import dk.cachet.carp.protocols.infrastructure.JSON


/**
 * Create a [StudyRuntimeSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyRuntimeSnapshot.Companion.fromJson( json: String ): StudyRuntimeSnapshot
{
    return JSON.decodeFromString( serializer(), json )
}

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyRuntimeSnapshot.toJson(): String
{
    return JSON.encodeToString( StudyRuntimeSnapshot.serializer(), this )
}
