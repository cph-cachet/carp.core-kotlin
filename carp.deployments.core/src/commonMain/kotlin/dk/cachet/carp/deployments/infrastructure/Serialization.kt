@file:Suppress( "TooManyFunctions" )

package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot


/**
 * Create a [Account] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Account.Companion.fromJson( json: String ): Account =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Account.toJson(): String =
    JSON.encodeToString( Account.serializer(), this )

/**
 * Create a [Username] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Username.Companion.fromJson( json: String ): Username =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Username.toJson(): String =
    JSON.encodeToString( Username.serializer(), this )

/**
 * Create a [Participation] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Participation.Companion.fromJson( json: String ): Participation =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Participation.toJson(): String =
    JSON.encodeToString( Participation.serializer(), this )

/**
 * Create a [StudyInvitation] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyInvitation.Companion.fromJson( json: String ): StudyInvitation =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyInvitation.toJson(): String =
    JSON.encodeToString( StudyInvitation.serializer(), this )

/**
 * Create a [StudyDeploymentSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentSnapshot.Companion.fromJson( json: String ): StudyDeploymentSnapshot =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentSnapshot.toJson(): String =
    JSON.encodeToString( StudyDeploymentSnapshot.serializer(), this )

/**
 * Create a [StudyDeploymentStatus] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentStatus.Companion.fromJson( json: String ): StudyDeploymentStatus =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentStatus.toJson(): String =
    JSON.encodeToString( StudyDeploymentStatus.serializer(), this )

/**
 * Create a [MasterDeviceDeployment] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun MasterDeviceDeployment.Companion.fromJson( json: String ): MasterDeviceDeployment =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun MasterDeviceDeployment.toJson(): String =
    JSON.encodeToString( MasterDeviceDeployment.serializer(), this )

/**
 * Create [ParticipantData] from JSON, serializer using the globally set infrastructure serializer ([JSON]).
 */
fun ParticipantData.Companion.fromJson( json: String ): ParticipantData =
    JSON.decodeFromString( serializer(), json )

/**
 * Serializer to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ParticipantData.toJson(): String =
    JSON.encodeToString( ParticipantData.serializer(), this )

/**
 * Create a [ParticipantGroupSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun ParticipantGroupSnapshot.Companion.fromJson( json: String ): ParticipantGroupSnapshot =
    JSON.decodeFromString( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun ParticipantGroupSnapshot.toJson(): String =
    JSON.encodeToString( ParticipantGroupSnapshot.serializer(), this )
