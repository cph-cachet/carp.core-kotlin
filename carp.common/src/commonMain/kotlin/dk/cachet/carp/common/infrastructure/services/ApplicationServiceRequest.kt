package dk.cachet.carp.common.infrastructure.services


/**
 * A request for [TService] stored in memory, which can be invoked using [invokeOn].
 */
interface ApplicationServiceRequest<TService, out TReturn>
{
    suspend fun invokeOn( service: TService ): TReturn
}
