package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.tasks.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*


/**
 * Types in the [dk.cachet.carp.protocols] module which need to be registered when using [Json] serializer.
 */
val PROTOCOLS_SERIAL_MODULE = SerializersModule {
    polymorphic( TaskDescriptor::class )
    {
        ConcurrentTask::class with ConcurrentTask.serializer()
    }
}

/**
 * Create a [Json] serializer adopting a default CARP infrastructure configuration with all [dk.cachet.carp.protocols] types registered.
 * This ensures a global configuration on how serialization should occur.
 * Additional types the serializer needs to be aware about (such as polymorph extending classes) should be registered through [module].
 */
fun createProtocolsSerializer( module: SerialModule = EmptyModule ): Json
{
    return createDefaultJSON( PROTOCOLS_SERIAL_MODULE + module )
}

/**
 * A default CARP infrastructure serializer capable of serializing all [dk.cachet.carp.protocols] types.
 * In case custom extending types are defined, this variable should be reassigned for serialization extension functions to work as expected.
 * [createProtocolsSerializer] can be used to this end, by including all extending types in the [SerialModule] as parameter.
 */
var JSON: Json = createProtocolsSerializer()


/**
 * Create a [StudyProtocolSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.Companion.fromJson( json: String ): StudyProtocolSnapshot
{
    return JSON.parse( serializer(), json )
}

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyProtocolSnapshot.toJson(): String
{
    return JSON.stringify( StudyProtocolSnapshot.serializer(), this )
}