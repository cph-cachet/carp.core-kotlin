package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService


/**
 * A proxy for [ApplicationService] which notifies of incoming requests and responses through [log].
 */
open class ApplicationServiceLog<TService : ApplicationService<TService, *>>(
    private val service: TService,
    private val log: (LoggedRequest<TService>) -> Unit
)
{
    /**
     * Execute the [request] and log it including the response.
     */
    protected suspend fun <TReturn> log( request: ServiceInvoker<TService, TReturn> ): TReturn
    {
        @Suppress( "TooGenericExceptionCaught" )
        val response =
            try { request.invokeOn( service ) }
            catch ( ex: Exception )
            {
                log( LoggedRequest.Failed( request, ex ) )
                throw ex
            }

        log( LoggedRequest.Succeeded( request, response ) )

        return response
    }
}


/**
 * An intercepted [request] and response to the application service [TService].
 */
sealed class LoggedRequest<TService>( val request: ServiceInvoker<TService, *> )
{
    /**
     * The intercepted [request] succeeded and returned [response].
     */
    class Succeeded<TService>( request: ServiceInvoker<TService, *>, val response: Any? ) :
        LoggedRequest<TService>( request )

    /**
     * The intercepted [request] failed with an [exception].
     */
    class Failed<TService>( request: ServiceInvoker<TService, *>, val exception: Exception ) :
        LoggedRequest<TService>( request )
}
