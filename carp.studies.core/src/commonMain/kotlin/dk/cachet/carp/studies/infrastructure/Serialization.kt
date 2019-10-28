package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.studies.domain.users.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*


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
fun Account.Companion.fromJson( json: String ): Account
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Account.toJson(): String
    = JSON.stringify( Account.serializer(), this )

/**
 * Create a [Participant] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun Participant.Companion.fromJson( json: String ): Participant
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun Participant.toJson(): String
    = JSON.stringify( Participant.serializer(), this )

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