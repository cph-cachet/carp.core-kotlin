@file:Suppress( "TooManyFunctions" )

package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.common.infrastructure.serialization.createProtocolsSerializer
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.domain.users.ParticipantData
import dk.cachet.carp.deployment.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule


/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.deployment] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createDeploymentSerializer( module: SerializersModule? = null ): Json = createProtocolsSerializer( module )

/**
 * A default CARP infrastructure serializer capable of serializing all [dk.cachet.carp.deployment] types.
 * In case custom extending types are defined, this variable should be reassigned for serialization extension functions to work as expected.
 * [createDeploymentSerializer] can be used to this end, by including all extending types in the [SerialModule] as parameter.
 */
var JSON: Json = createDeploymentSerializer()

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
