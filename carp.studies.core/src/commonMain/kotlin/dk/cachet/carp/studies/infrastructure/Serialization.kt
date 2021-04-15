@file:Suppress( "TooManyFunctions" )

package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.studies.application.users.AssignParticipantDevices
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.StudyOwner
import dk.cachet.carp.studies.domain.StudySnapshot


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
