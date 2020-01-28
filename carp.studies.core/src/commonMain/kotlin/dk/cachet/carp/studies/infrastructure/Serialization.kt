@file:Suppress( "TooManyFunctions" )

package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.studies.domain.StudyDescription
import dk.cachet.carp.studies.domain.StudyOwner
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.Account
import dk.cachet.carp.studies.domain.users.AccountIdentity
import dk.cachet.carp.studies.domain.users.EmailAccountIdentity
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.Username
import dk.cachet.carp.studies.domain.users.UsernameAccountIdentity
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerialModule


/**
 * Types in the [dk.cachet.carp.studies] module which need to be registered when using [Json] serializer.
 */
val STUDIES_SERIAL_MODULE = SerializersModule {
    polymorphic( AccountIdentity::class )
    {
        UsernameAccountIdentity::class with UsernameAccountIdentity.serializer()
        EmailAccountIdentity::class with EmailAccountIdentity.serializer()
    }
}

/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.studies] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createStudiesSerializer( module: SerialModule = EmptyModule ): Json
{
    return createDefaultJSON( STUDIES_SERIAL_MODULE + module )
}

/**
 * A default CARP infrastructure serializer capable of serializing all [dk.cachet.carp.studies] types.
 * In case custom extending types are defined, this variable should be reassigned for serialization extension functions to work as expected.
 * [createStudiesSerializer] can be used to this end, by including all extending types in the [SerialModule] as parameter.
 */
var JSON: Json = createStudiesSerializer()

/**
 * Create a [Account] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Account.Companion.fromJson( json: String ): Account =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Account.toJson(): String =
    JSON.stringify( Account.serializer(), this )

/**
 * Create a [Username] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Username.Companion.fromJson( json: String ): Username =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Username.toJson(): String =
    JSON.stringify( Username.serializer(), this )

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
 * Create a [StudyDescription] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDescription.Companion.fromJson( json: String ): StudyDescription =
    JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDescription.toJson(): String =
    JSON.stringify( StudyDescription.serializer(), this )

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
