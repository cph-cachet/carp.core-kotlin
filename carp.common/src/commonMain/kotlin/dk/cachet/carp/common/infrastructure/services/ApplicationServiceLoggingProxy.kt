package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.reflect.AccessInternals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonClassDiscriminator


/**
 * A proxy for [ApplicationService] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests] and published events in [loggedEvents].
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
        @Suppress( "TooGenericExceptionCaught" )
        val response =
            try { request.invokeOn( service ) }
            catch ( ex: Exception )
            {
                addLog( LoggedRequest.Failed( request, eventBusLog.retrieveAndEmptyLog(), ex::class.simpleName!! ) )
                throw ex
            }

        addLog( LoggedRequest.Succeeded( request, eventBusLog.retrieveAndEmptyLog(), response ) )

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
@Serializable( LoggedRequestSerializer::class )
sealed interface LoggedRequest<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>>
{
    val request: ApplicationServiceRequest<TService, *>
    val events: List<IntegrationEvent<*>>

    /**
     * The intercepted [request] succeeded and returned [response].
     */
    data class Succeeded<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>>(
        override val request: ApplicationServiceRequest<TService, *>,
        override val events: List<IntegrationEvent<*>>,
        val response: Any?
    ) : LoggedRequest<TService, TEvent>

    /**
     * The intercepted [request] failed with an exception of [exceptionType].
     */
    data class Failed<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>>(
        override val request: ApplicationServiceRequest<TService, *>,
        override val events: List<IntegrationEvent<*>>,
        val exceptionType: String
    ) : LoggedRequest<TService, TEvent>
}


/**
 * Serializer for [LoggedRequest]s of [TService].
 */
@OptIn( InternalSerializationApi::class )
class LoggedRequestSerializer<TService : ApplicationService<TService, *>>(
    /**
     * The request serializer for [TService] which can polymorphically serialize any of its requests.
     */
    requestSerializer: KSerializer<out ApplicationServiceRequest<*, *>>, // TODO: Specify TService here, preventing casts.
    /**
     * A serializer for any of the events that may be received or are published by [TService].
     */
    eventSerializer: KSerializer<out IntegrationEvent<*>>
) : KSerializer<LoggedRequest<*, *>>
{
    private val eventsSerializer = ListSerializer( eventSerializer )

    private val succeededSerializer =
        object : KSerializer<LoggedRequest.Succeeded<*, *>>
        {
            private val responseSerialDescriptor = buildClassSerialDescriptor(
                "${LoggedRequestSerializer::class.simpleName!!}\$Response"
            )

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Succeeded::class.simpleName!! )
                {
                    element( "request", requestSerializer.descriptor )
                    element( "events", eventsSerializer.descriptor )
                    element( "response", responseSerialDescriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Succeeded<*, *> )
            {
                val responseSerializer = value.request.getResponseSerializer() as KSerializer<Any?>

                encoder.encodeStructure( descriptor )
                {
                    encodeSerializableElement( descriptor, 0, requestSerializer as KSerializer<Any>, value.request )
                    encodeSerializableElement( descriptor, 1, eventsSerializer as KSerializer<Any>, value.events )
                    encodeSerializableElement( descriptor, 2, responseSerializer, value.response )
                }
            }

            @Suppress( "UNCHECKED_CAST" )
            override fun deserialize( decoder: Decoder ): LoggedRequest.Succeeded<*, *>
            {
                var request: ApplicationServiceRequest<*, *>? = null
                var events: List<IntegrationEvent<*>>? = null
                var response: Any? = null
                decoder.decodeStructure( descriptor )
                {
                    request = decodeSerializableElement( descriptor, 0, requestSerializer )
                    events = decodeSerializableElement( descriptor, 1, eventsSerializer )
                    response = decodeSerializableElement( descriptor, 2, request!!.getResponseSerializer() )
                }

                return LoggedRequest.Succeeded(
                    checkNotNull( request ) as ApplicationServiceRequest<TService, *>,
                    checkNotNull( events ) as List<IntegrationEvent<TService>>,
                    response
                )
            }
        }

    private val failedSerializer =
        object : KSerializer<LoggedRequest.Failed<*, *>>
        {
            private val exceptionSerializer = serializer<String>()

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Failed::class.simpleName!! )
                {
                    element( "request", requestSerializer.descriptor )
                    element( "events", requestSerializer.descriptor )
                    element( "exception", exceptionSerializer.descriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Failed<*, *> )
            {
                encoder.encodeStructure( descriptor )
                {
                    encodeSerializableElement( descriptor, 0, requestSerializer as KSerializer<Any>, value.request )
                    encodeSerializableElement( descriptor, 1, eventsSerializer as KSerializer<Any>, value.events )
                    encodeSerializableElement( descriptor, 2, exceptionSerializer, value.exceptionType )
                }
            }

            @Suppress( "UNCHECKED_CAST" )
            override fun deserialize( decoder: Decoder ): LoggedRequest.Failed<*, *>
            {
                var request: ApplicationServiceRequest<*, *>? = null
                var events: List<IntegrationEvent<*>>? = null
                var exceptionType: String? = null
                decoder.decodeStructure( descriptor )
                {
                    request = decodeSerializableElement( descriptor, 0, requestSerializer )
                    events = decodeSerializableElement( descriptor, 1, eventsSerializer )
                    exceptionType = decodeSerializableElement( descriptor, 2, exceptionSerializer )
                }

                return LoggedRequest.Failed(
                    checkNotNull( request ) as ApplicationServiceRequest<TService, *>,
                    checkNotNull( events ) as List<IntegrationEvent<TService>>,
                    checkNotNull( exceptionType )
                )
            }
        }


    @OptIn( ExperimentalSerializationApi::class )
    private val sealedSerializer = SealedClassSerializer(
        LoggedRequest::class.simpleName!!,
        LoggedRequest::class,
        arrayOf( LoggedRequest.Succeeded::class, LoggedRequest.Failed::class ),
        arrayOf( succeededSerializer, failedSerializer )
    ).also {
        // HACK: Change class discriminator so that it does not depend on JsonConfiguration.
        //   For now the secondary constructor which allows setting annotations is internal; it may become public later.
        AccessInternals.setField( it, "_annotations", listOf( JsonClassDiscriminator("outcome" ) ) )
    }

    override val descriptor: SerialDescriptor = sealedSerializer.descriptor

    override fun serialize( encoder: Encoder, value: LoggedRequest<*, *> ) =
        encoder.encodeSerializableValue( sealedSerializer, value )

    override fun deserialize( decoder: Decoder ): LoggedRequest<*, *> =
        decoder.decodeSerializableValue( sealedSerializer )
}
