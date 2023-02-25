package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService


/**
 * Base class to help apply a [requestDecorator] on all requests of a [service].
 * Extend from this class and implement the application service interface by
 * redirecting all requests to [invoke] and initializing the matching [TRequest].
 */
open class ApplicationServiceDecorator<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    private val service: TService,
    private val requestInvoker: ApplicationServiceInvoker<TService, TRequest>,
    private val requestDecorator: (Command<TRequest>) -> Command<TRequest>
) : Command<TRequest>
{
    private val invokeService =
        object : Command<TRequest>
        {
            @Suppress( "UNCHECKED_CAST" )
            override suspend fun <TReturn> invoke( request: TRequest ): TReturn =
                requestInvoker.invokeOnService( request, service ) as TReturn
        }

    override suspend fun <TReturn> invoke( request: TRequest ): TReturn =
        requestDecorator( invokeService ).invoke( request ) as TReturn
}


/**
 * Supports invocation of a [TRequest] on a predefined service.
 */
interface Command<TRequest>
{
    suspend fun <TReturn> invoke( request: TRequest ): TReturn
}
