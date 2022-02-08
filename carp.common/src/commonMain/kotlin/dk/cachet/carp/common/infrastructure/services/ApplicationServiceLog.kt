package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer


/**
 * A proxy for [ApplicationService] which notifies of incoming requests and responses through [log]
 * and keeps a history of requests in [loggedRequests].
 */
open class ApplicationServiceLog<TService : ApplicationService<TService, *>>(
    private val service: TService,
    private val log: (LoggedRequest<TService>) -> Unit = { }
)
{
    private val _loggedRequests: MutableList<LoggedRequest<TService>> = mutableListOf()
    val loggedRequests: List<LoggedRequest<TService>>
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
                addLog( LoggedRequest.Failed( request, ex::class.simpleName!! ) )
                throw ex
            }

        addLog( LoggedRequest.Succeeded( request, response ) )

        return response
    }

    private fun addLog( loggedRequest: LoggedRequest<TService> )
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
 * An intercepted [request] and response to an application service identified by [serviceKlass].
 */
@Serializable( LoggedRequestSerializer::class )
sealed interface LoggedRequest<TService : ApplicationService<TService, *>>
{
    val request: ApplicationServiceRequest<TService, *>

    /**
     * The intercepted [request] succeeded and returned [response].
     */
    data class Succeeded<TService : ApplicationService<TService, *>>(
        override val request: ApplicationServiceRequest<TService, *>,
        val response: Any?
    ) : LoggedRequest<TService>

    /**
     * The intercepted [request] failed with an [exception].
     */
    data class Failed<TService : ApplicationService<TService, *>>(
        override val request: ApplicationServiceRequest<TService, *>,
        val exceptionType: String
    ) : LoggedRequest<TService>
}


/**
 * Serializer for [LoggedRequest]s of [TService].
 */
@OptIn( InternalSerializationApi::class )
class LoggedRequestSerializer<TService : ApplicationService<TService, *>>(
    /**
     * The request serializer for [TService] which can polymorphically serialize any of its requests.
     */
    val requestSerializer: KSerializer<out ApplicationServiceRequest<*, *>> // TODO: Specify TService here, preventing casts.
) : KSerializer<LoggedRequest<*>>
{
    private val succeededSerializer =
        object : KSerializer<LoggedRequest.Succeeded<*>>
        {
            private val responseSerialDescriptor = buildClassSerialDescriptor(
                "${LoggedRequestSerializer::class.simpleName!!}\$Response"
            )

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Succeeded::class.simpleName!! )
                {
                    element( "request", requestSerializer.descriptor )
                    element( "response", responseSerialDescriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Succeeded<*> )
            {
                val responseSerializer = value.request.getResponseSerializer() as KSerializer<Any?>

                encoder.encodeStructure( descriptor )
                {
                    encodeSerializableElement( descriptor, 0, requestSerializer as KSerializer<Any>, value.request )
                    encodeSerializableElement( descriptor, 1, responseSerializer, value.response )
                }
            }

            @Suppress( "UNCHECKED_CAST" )
            override fun deserialize( decoder: Decoder ): LoggedRequest.Succeeded<*>
            {
                var request: ApplicationServiceRequest<*, *>? = null
                var response: Any? = null
                decoder.decodeStructure( descriptor )
                {
                    request = decodeSerializableElement( descriptor, 0, requestSerializer )
                    response = decodeSerializableElement( descriptor, 1, request!!.getResponseSerializer() )
                }

                return LoggedRequest.Succeeded(
                    checkNotNull( request ) as ApplicationServiceRequest<TService, *>,
                    response
                )
            }
        }

    private val failedSerializer =
        object : KSerializer<LoggedRequest.Failed<*>>
        {
            private val exceptionSerializer = serializer<String>()

            override val descriptor: SerialDescriptor =
                buildClassSerialDescriptor( LoggedRequest.Failed::class.simpleName!! )
                {
                    element( "request", requestSerializer.descriptor )
                    element( "exception", exceptionSerializer.descriptor )
                }

            @Suppress( "UNCHECKED_CAST" )
            override fun serialize( encoder: Encoder, value: LoggedRequest.Failed<*> )
            {
                encoder.encodeStructure( descriptor )
                {
                    encodeSerializableElement( descriptor, 0, requestSerializer as KSerializer<Any>, value.request )
                    encodeSerializableElement( descriptor, 1, exceptionSerializer, value.exceptionType )
                }
            }

            @Suppress( "UNCHECKED_CAST" )
            override fun deserialize( decoder: Decoder ): LoggedRequest.Failed<*>
            {
                var request: ApplicationServiceRequest<*, *>? = null
                var exceptionType: String? = null
                decoder.decodeStructure( descriptor )
                {
                    request = decodeSerializableElement( descriptor, 0, requestSerializer )
                    exceptionType = decodeSerializableElement( descriptor, 1, exceptionSerializer )
                }

                return LoggedRequest.Failed(
                    checkNotNull( request ) as ApplicationServiceRequest<TService, *>,
                    checkNotNull( exceptionType )
                )
            }
        }


    private val sealedSerializer = SealedClassSerializer(
        LoggedRequest::class.simpleName!!,
        LoggedRequest::class,
        arrayOf( LoggedRequest.Succeeded::class, LoggedRequest.Failed::class ),
        arrayOf( succeededSerializer, failedSerializer )
    )

    override val descriptor: SerialDescriptor = sealedSerializer.descriptor

    override fun serialize( encoder: Encoder, value: LoggedRequest<*> ) =
        encoder.encodeSerializableValue( sealedSerializer, value )

    override fun deserialize( decoder: Decoder ): LoggedRequest<*> =
        decoder.decodeSerializableValue( sealedSerializer )
}
