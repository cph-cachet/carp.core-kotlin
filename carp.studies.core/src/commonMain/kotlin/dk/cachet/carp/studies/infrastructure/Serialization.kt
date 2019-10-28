package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.protocols.infrastructure.JSON


/**
 * Create a [StudySnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudySnapshot.Companion.fromJson( json: String ): StudySnapshot
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudySnapshot.toJson(): String
    = JSON.stringify( StudySnapshot.serializer(), this )