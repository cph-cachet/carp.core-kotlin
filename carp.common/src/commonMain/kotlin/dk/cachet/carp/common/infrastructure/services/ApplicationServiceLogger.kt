package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.reflect.AccessInternals
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.JsonClassDiscriminator


/**
 * Access [loggedRequests] of an [ApplicationService].
 */
class ApplicationServiceLogger<
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>
>
{
    private val _loggedRequests: MutableList<LoggedRequest<TService>> = mutableListOf()
    val loggedRequests: List<LoggedRequest<TService>>
        get() = _loggedRequests.toList()

    fun addLog( loggedRequest: LoggedRequest<TService> ) = _loggedRequests.add( loggedRequest )

    /**
     * Determines whether the given [request] is present in [loggedRequests].
     */
    fun wasCalled( request: ApplicationServiceRequest<TService, *> ): Boolean =
        _loggedRequests.map { it.request }.contains( request )

    /**
     * Clear the current [loggedRequests].
     */
    fun clear() = _loggedRequests.clear()
}


/**
 * An intercepted [request] and response to an application service [TService].
 */
sealed interface LoggedRequest<TService : ApplicationService<TService, *>>
{
    val request: ApplicationServiceRequest<TService, *>
    val precedingEvents: List<IntegrationEvent<*>>
    val publishedEvents: List<IntegrationEvent<*>>

    /**
     * The intercepted [request] succeeded and returned [response].
     */
    data class Succeeded<TService : ApplicationService<TService, *>>(
        override val request: ApplicationServiceRequest<TService, *>,
        override val precedingEvents: List<IntegrationEvent<*>>,
        override val publishedEvents: List<IntegrationEvent<*>>,
        val response: Any?
    ) : LoggedRequest<TService>

    /**
     * The intercepted [request] failed with an exception of [exceptionType].
     */
    data class Failed<TService : ApplicationService<TService, *>>(
        override val request: ApplicationServiceRequest<TService, *>,
        override val precedingEvents: List<IntegrationEvent<*>>,
        override val publishedEvents: List<IntegrationEvent<*>>,
        val exceptionType: String
    ) : LoggedRequest<TService>
}


/**
 * Serializer for [LoggedRequest]s of [TService].
 */
@OptIn( ExperimentalSerializationApi::class, InternalSerializationApi::class )
class LoggedRequestSerializer<TService : ApplicationService<TService, *>>(
    /**
     * The request serializer for [TService] which can polymorphically serialize any of its requests.
     */
    requestSerializer: KSerializer<out ApplicationServiceRequest<TService, *>>,
    /**
     * A serializer for any of the events that may be received or are published by [TService].
     */
    eventSerializer: KSerializer<out IntegrationEvent<*>>
) : KSerializer<LoggedRequest<*>>
{
    private val eventsSerializer = ListSerializer( eventSerializer )

    @Suppress( "MagicNumber" )
    private val succeededSerializer =
        object : KSerializer<LoggedRequest.Succeeded<*>>
        {
            private val responseSerialDescriptor = buildClassSerialDescriptor(
                "${LoggedRequestSerializer::class.simpleName!!}\$Response"
            )

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Succeeded::class.simpleName!! )
                {
                    element( LoggedRequest<*>::request.name, requestSerializer.descriptor )
                    element( LoggedRequest<*>::precedingEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest<*>::publishedEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest.Succeeded<*>::response.name, responseSerialDescriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Succeeded<*> )
            {
                val responseSerializer = value.request.getResponseSerializer() as KSerializer<Any?>
                val anyEventsSerializer = eventsSerializer as KSerializer<Any>

                encoder.encodeStructure( descriptor )
                {
                    encodeSerializableElement( descriptor, 0, requestSerializer as KSerializer<Any>, value.request )
                    encodeSerializableElement( descriptor, 1, anyEventsSerializer, value.precedingEvents )
                    encodeSerializableElement( descriptor, 2, anyEventsSerializer, value.publishedEvents )
                    encodeSerializableElement( descriptor, 3, responseSerializer, value.response )
                }
            }

            override fun deserialize( decoder: Decoder ): LoggedRequest.Succeeded<*> =
                decoder.decodeStructure( descriptor )
                {
                    var request: ApplicationServiceRequest<TService, *>? = null
                    var precedingEvents: List<IntegrationEvent<*>>? = null
                    var publishedEvents: List<IntegrationEvent<*>>? = null
                    var response: Any? = null

                    var decoding = true
                    while ( decoding )
                    {
                        when ( val index = decodeElementIndex( descriptor ) )
                        {
                            0 -> request = decodeSerializableElement( descriptor, 0, requestSerializer )
                            1 -> precedingEvents = decodeSerializableElement( descriptor, 1, eventsSerializer )
                            2 -> publishedEvents = decodeSerializableElement( descriptor, 2, eventsSerializer )
                            3 ->
                            {
                                val responseSerializer = checkNotNull( request ).getResponseSerializer()
                                response = decodeSerializableElement( descriptor, 3, responseSerializer )
                            }
                            CompositeDecoder.DECODE_DONE -> decoding = false
                            else -> error( "Unexpected index: $index" )
                        }
                    }

                    LoggedRequest.Succeeded(
                        checkNotNull( request ),
                        checkNotNull( precedingEvents ),
                        checkNotNull( publishedEvents ),
                        response
                    )
                }
        }

    @Suppress( "MagicNumber" )
    private val failedSerializer =
        object : KSerializer<LoggedRequest.Failed<*>>
        {
            private val exceptionSerializer = serializer<String>()

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Failed::class.simpleName!! )
                {
                    element( LoggedRequest<*>::request.name, requestSerializer.descriptor )
                    element( LoggedRequest<*>::precedingEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest<*>::publishedEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest.Failed<*>::exceptionType.name, exceptionSerializer.descriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Failed<*> )
            {
                val anyEventsSerializer = eventsSerializer as KSerializer<Any>

                encoder.encodeStructure( descriptor )
                {
                    encodeSerializableElement( descriptor, 0, requestSerializer as KSerializer<Any>, value.request )
                    encodeSerializableElement( descriptor, 1, anyEventsSerializer, value.precedingEvents )
                    encodeSerializableElement( descriptor, 2, anyEventsSerializer, value.publishedEvents )
                    encodeSerializableElement( descriptor, 3, exceptionSerializer, value.exceptionType )
                }
            }

            override fun deserialize( decoder: Decoder ): LoggedRequest.Failed<*> =
                decoder.decodeStructure( descriptor )
                {
                    var request: ApplicationServiceRequest<TService, *>? = null
                    var precedingEvents: List<IntegrationEvent<*>>? = null
                    var publishedEvents: List<IntegrationEvent<*>>? = null
                    var exceptionType: String? = null

                    var decoding = true
                    while ( decoding )
                    {
                        when ( val index = decodeElementIndex( descriptor ) )
                        {
                            0 -> request = decodeSerializableElement( descriptor, 0, requestSerializer )
                            1 -> precedingEvents = decodeSerializableElement( descriptor, 1, eventsSerializer )
                            2 -> publishedEvents = decodeSerializableElement( descriptor, 2, eventsSerializer )
                            3 -> exceptionType = decodeSerializableElement( descriptor, 3, exceptionSerializer )
                            CompositeDecoder.DECODE_DONE -> decoding = false
                            else -> error( "Unexpected index: $index" )
                        }
                    }

                    LoggedRequest.Failed(
                        checkNotNull( request ),
                        checkNotNull( precedingEvents ),
                        checkNotNull( publishedEvents ),
                        checkNotNull( exceptionType )
                    )
                }
        }


    private val sealedSerializer = SealedClassSerializer(
        LoggedRequest::class.simpleName!!,
        LoggedRequest::class,
        arrayOf( LoggedRequest.Succeeded::class, LoggedRequest.Failed::class ),
        arrayOf( succeededSerializer, failedSerializer )
    ).apply {
        // HACK: Change class discriminator so that it does not depend on JsonConfiguration.
        //   For now the secondary constructor which allows setting annotations is internal; it may become public later.
        AccessInternals.setField( this, "_annotations", listOf( JsonClassDiscriminator("outcome" ) ) )
    }

    override val descriptor: SerialDescriptor = sealedSerializer.descriptor

    override fun serialize( encoder: Encoder, value: LoggedRequest<*> ) =
        encoder.encodeSerializableValue( sealedSerializer, value )

    override fun deserialize( decoder: Decoder ): LoggedRequest<*> =
        decoder.decodeSerializableValue( sealedSerializer )
}
