package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.reflect.AccessInternals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.serializer


/**
 * A proxy for [ApplicationService] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests and published events in [loggedRequests].
 */
open class ApplicationServiceLoggingProxy<
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>
>(
    private val service: TService,
    private val eventBusLog: EventBusLog,
    private val log: (LoggedRequest<TService, TEvent>) -> Unit = { }
)
{
    private val _loggedRequests: MutableList<LoggedRequest<TService, TEvent>> = mutableListOf()
    val loggedRequests: List<LoggedRequest<TService, TEvent>>
        get() = _loggedRequests.toList()

    /**
     * Execute the [request] and log it including the response.
     */
    protected suspend fun <TReturn> log( request: ApplicationServiceRequest<TService, TReturn> ): TReturn
    {
        @Suppress( "UNCHECKED_CAST" )
        fun getCurrentEvents() = eventBusLog.retrieveAndEmptyLog() as List<TEvent>
        val precedingEvents = getCurrentEvents()

        @Suppress( "TooGenericExceptionCaught" )
        val response =
            try { request.invokeOn( service ) }
            catch ( ex: Exception )
            {
                val failed = LoggedRequest.Failed(
                    request,
                    precedingEvents,
                    getCurrentEvents(),
                    ex::class.simpleName!!
                )
                addLog( failed )
                throw ex
            }

        addLog( LoggedRequest.Succeeded( request, precedingEvents, getCurrentEvents(), response ) )

        return response
    }

    private fun addLog( loggedRequest: LoggedRequest<TService, TEvent> )
    {
        _loggedRequests.add( loggedRequest )
        log( loggedRequest )
    }

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
sealed interface LoggedRequest<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>>
{
    val request: ApplicationServiceRequest<TService, *>
    val precedingEvents: List<TEvent>
    val publishedEvents: List<TEvent>

    /**
     * The intercepted [request] succeeded and returned [response].
     */
    data class Succeeded<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>>(
        override val request: ApplicationServiceRequest<TService, *>,
        override val precedingEvents: List<TEvent>,
        override val publishedEvents: List<TEvent>,
        val response: Any?
    ) : LoggedRequest<TService, TEvent>

    /**
     * The intercepted [request] failed with an exception of [exceptionType].
     */
    data class Failed<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>>(
        override val request: ApplicationServiceRequest<TService, *>,
        override val precedingEvents: List<TEvent>,
        override val publishedEvents: List<TEvent>,
        val exceptionType: String
    ) : LoggedRequest<TService, TEvent>
}


/**
 * Serializer for [LoggedRequest]s of [TService].
 */
@OptIn( ExperimentalSerializationApi::class, InternalSerializationApi::class )
class LoggedRequestSerializer<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>>(
    /**
     * The request serializer for [TService] which can polymorphically serialize any of its requests.
     */
    requestSerializer: KSerializer<out ApplicationServiceRequest<TService, *>>,
    /**
     * A serializer for any of the events that may be received or are published by [TService].
     */
    eventSerializer: KSerializer<out TEvent>
) : KSerializer<LoggedRequest<*, *>>
{
    private val eventsSerializer = ListSerializer( eventSerializer )

    @Suppress( "MagicNumber" )
    private val succeededSerializer =
        object : KSerializer<LoggedRequest.Succeeded<*, *>>
        {
            private val responseSerialDescriptor = buildClassSerialDescriptor(
                "${LoggedRequestSerializer::class.simpleName!!}\$Response"
            )

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Succeeded::class.simpleName!! )
                {
                    element( LoggedRequest<*, *>::request.name, requestSerializer.descriptor )
                    element( LoggedRequest<*, *>::precedingEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest<*, *>::publishedEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest.Succeeded<*, *>::response.name, responseSerialDescriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Succeeded<*, *> )
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

            @Suppress( "UNCHECKED_CAST" )
            override fun deserialize( decoder: Decoder ): LoggedRequest.Succeeded<*, *> =
                decoder.decodeStructure( descriptor )
                {
                    var request: ApplicationServiceRequest<TService, *>? = null
                    var precedingEvents: List<TEvent>? = null
                    var publishedEvents: List<TEvent>? = null
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
        object : KSerializer<LoggedRequest.Failed<*, *>>
        {
            private val exceptionSerializer = serializer<String>()

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Failed::class.simpleName!! )
                {
                    element( LoggedRequest<*, *>::request.name, requestSerializer.descriptor )
                    element( LoggedRequest<*, *>::precedingEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest<*, *>::publishedEvents.name, eventsSerializer.descriptor )
                    element( LoggedRequest.Failed<*, *>::exceptionType.name, exceptionSerializer.descriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Failed<*, *> )
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

            @Suppress( "UNCHECKED_CAST" )
            override fun deserialize( decoder: Decoder ): LoggedRequest.Failed<*, *> =
                decoder.decodeStructure( descriptor )
                {
                    var request: ApplicationServiceRequest<TService, *>? = null
                    var precedingEvents: List<TEvent>? = null
                    var publishedEvents: List<TEvent>? = null
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

    override fun serialize( encoder: Encoder, value: LoggedRequest<*, *> ) =
        encoder.encodeSerializableValue( sealedSerializer, value )

    override fun deserialize( decoder: Decoder ): LoggedRequest<*, *> =
        decoder.decodeSerializableValue( sealedSerializer )
}
