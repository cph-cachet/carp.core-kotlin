@file:Suppress( "TooManyFunctions" )

package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.protocols.infrastructure.createProtocolsSerializer
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule


/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.studies] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createStudiesSerializer( module: SerializersModule? = null ): Json = createProtocolsSerializer( module )

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
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudySnapshot.toJson(): String =
    JSON.encodeToString( StudySnapshot.serializer(), this )

/**
 * Create a [Participant] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Participant.Companion.fromJson( json: String ): Participant =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Participant.toJson(): String =
    JSON.encodeToString( Participant.serializer(), this )

/**
 * Create a [StudyOwner] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyOwner.Companion.fromJson( json: String ): StudyOwner =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyOwner.toJson(): String =
    JSON.encodeToString( StudyOwner.serializer(), this )

/**
 * Create a [StudyStatus] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyStatus.Companion.fromJson( json: String ): StudyStatus =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyStatus.toJson(): String =
    JSON.encodeToString( StudyStatus.serializer(), this )

/**
 * Create a [AssignParticipantDevices] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun AssignParticipantDevices.Companion.fromJson( json: String ): AssignParticipantDevices =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun AssignParticipantDevices.toJson(): String =
    JSON.encodeToString( AssignParticipantDevices.serializer(), this )
