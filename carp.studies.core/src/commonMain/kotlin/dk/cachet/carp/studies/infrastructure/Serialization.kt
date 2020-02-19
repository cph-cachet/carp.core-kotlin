@file:Suppress( "TooManyFunctions" )

package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.infrastructure.PROTOCOLS_SERIAL_MODULE
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.Participant
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.SerialModule


/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.studies] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createStudiesSerializer( module: SerialModule = EmptyModule ): Json
{
    return createDefaultJSON( PROTOCOLS_SERIAL_MODULE + module )
}

/**
 * A default CARP infrastructure serializer capable of serializing all [dk.cachet.carp.studies] types.
 * In case custom extending types are defined, this variable should be reassigned for serialization extension functions to work as expected.
 * [createStudiesSerializer] can be used to this end, by including all extending types in the [SerialModule] as parameter.
 */
var JSON: Json = createStudiesSerializer()

/**
 * Create a [StudySnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudySnapshot.Companion.fromJson( json: String ): StudySnapshot =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudySnapshot.toJson(): String =
    JSON.stringify( StudySnapshot.serializer(), this )

/**
 * Create a [Participant] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Participant.Companion.fromJson( json: String ): Participant =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Participant.toJson(): String =
    JSON.stringify( Participant.serializer(), this )

/**
 * Create a [StudyOwner] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyOwner.Companion.fromJson( json: String ): StudyOwner =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyOwner.toJson(): String =
    JSON.stringify( StudyOwner.serializer(), this )

/**
 * Create a [StudyStatus] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyStatus.Companion.fromJson( json: String ): StudyStatus =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyStatus.toJson(): String =
    JSON.stringify( StudyStatus.serializer(), this )
