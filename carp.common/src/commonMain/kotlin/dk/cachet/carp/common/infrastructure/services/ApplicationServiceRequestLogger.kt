package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent


/**
 * Notifies of incoming requests and responses through [log],
 * as well as events preceding the request and fired as a result of the request.
 */
class ApplicationServiceRequestLogger<
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    private val eventBusLog: EventBusLog,
    private val log: (LoggedRequest<TService, TEvent>) -> Unit = { },
    private val decoratee: Command<TRequest>
) : Command<TRequest>
{
    override suspend fun <TReturn> invoke( request: TRequest ): TReturn
    {
        @Suppress( "UNCHECKED_CAST" )
        fun getCurrentEvents() = eventBusLog.retrieveAndEmptyLog() as List<TEvent>
        val precedingEvents = getCurrentEvents()

        @Suppress( "TooGenericExceptionCaught" )
        val response =
            try { decoratee.invoke( request ) as TReturn }
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
