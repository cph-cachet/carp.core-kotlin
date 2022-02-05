package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService


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
                addLog( LoggedRequest.Failed( request, ex ) )
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
 * An intercepted [request] and response to the application service [TService].
 */
sealed class LoggedRequest<TService>( val request: ApplicationServiceRequest<TService, *> )
{
    /**
     * The intercepted [request] succeeded and returned [response].
     */
    class Succeeded<TService>( request: ApplicationServiceRequest<TService, *>, val response: Any? ) :
        LoggedRequest<TService>( request )

    /**
     * The intercepted [request] failed with an [exception].
     */
    class Failed<TService>( request: ApplicationServiceRequest<TService, *>, val exception: Exception ) :
        LoggedRequest<TService>( request )
}
