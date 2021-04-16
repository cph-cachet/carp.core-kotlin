package dk.cachet.carp.clients.infrastructure

import dk.cachet.carp.clients.domain.StudyRuntimeSnapshot
import dk.cachet.carp.common.infrastructure.serialization.JSON


/**
 * Create a [StudyRuntimeSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyRuntimeSnapshot.Companion.fromJson( json: String ): StudyRuntimeSnapshot =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyRuntimeSnapshot.toJson(): String =
    JSON.encodeToString( StudyRuntimeSnapshot.serializer(), this )
