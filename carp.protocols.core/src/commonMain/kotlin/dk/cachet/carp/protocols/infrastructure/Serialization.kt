package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.ProtocolOwner


/**
 * Create a [ProtocolVersion] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolVersion.Companion.fromJson( json: String ): ProtocolVersion =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolVersion.toJson(): String =
    JSON.encodeToString( ProtocolVersion.serializer(), this )

/**
 * Create a [ProtocolOwner] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolOwner.Companion.fromJson( json: String ): ProtocolOwner =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ProtocolOwner.toJson(): String =
    JSON.encodeToString( ProtocolOwner.serializer(), this )

/**
 * Create a [StudyProtocolSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.Companion.fromJson( json: String ): StudyProtocolSnapshot =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.toJson(): String =
    JSON.encodeToString( StudyProtocolSnapshot.serializer(), this )
