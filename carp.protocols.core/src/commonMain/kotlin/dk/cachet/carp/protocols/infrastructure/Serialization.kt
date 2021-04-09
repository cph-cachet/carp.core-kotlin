package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.common.infrastructure.serialization.JSON


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
