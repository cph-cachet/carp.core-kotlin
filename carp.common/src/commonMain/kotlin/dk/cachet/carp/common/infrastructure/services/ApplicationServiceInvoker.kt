package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService


/**
 * Support invoking [TRequest] on a specified [TService].
 */
interface ApplicationServiceInvoker<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>
{
    suspend fun TRequest.invoke( service: TService ): Any?
    suspend fun invokeOnService( request: TRequest, service: TService ) = request.invoke( service )
}
