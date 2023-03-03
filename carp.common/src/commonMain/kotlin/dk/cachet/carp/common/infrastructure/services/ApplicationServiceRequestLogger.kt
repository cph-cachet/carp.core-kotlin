package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService


/**
 * Notifies of incoming requests and responses through [log],
 * as well as events preceding the request and fired as a result of the request.
 */
class ApplicationServiceRequestLogger<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    private val eventBusLog: EventBusLog,
    private val log: (LoggedRequest<TService>) -> Unit = { },
    private val decoratee: Command<TRequest>
) : Command<TRequest>
{
    override suspend fun invoke( request: TRequest ): Any?
    {
        fun getCurrentEvents() = eventBusLog.retrieveAndEmptyLog()
        val precedingEvents = getCurrentEvents()

        @Suppress( "TooGenericExceptionCaught" )
        val response =
            try { decoratee.invoke( request ) }
            catch ( ex: Exception )
            {
                val failed = LoggedRequest.Failed(
                    request,
                    precedingEvents,
                    getCurrentEvents(),
                    ex::class.simpleName!!
                )
                log( failed )
                throw ex
            }

        log( LoggedRequest.Succeeded( request, precedingEvents, getCurrentEvents(), response ) )

        return response
    }
}
