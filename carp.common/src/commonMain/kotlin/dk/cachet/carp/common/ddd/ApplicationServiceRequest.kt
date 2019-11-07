package dk.cachet.carp.common.ddd


/**
 * Represents a request to an application service of type [TService].
 */
interface ApplicationServiceRequest<TService, TReturn>
{
    /**
     * Execute this request on the specified application [service].
     */
    suspend fun executeOn( service: TService ): TReturn
}

/**
 * Store a specific request to an application service of type [TService] in memory.
 */
internal class InMemoryApplicationServiceRequest<TService, TReturn>( val invoke: suspend TService.() -> TReturn )
    : ApplicationServiceRequest<TService, TReturn>
{
    override suspend fun executeOn( service: TService ): TReturn = invoke( service )
}

/**
 * Create an application service request for [TService] which can be executed on demand.
 */
fun <TService, TReturn> createRequest( invoke: suspend TService.() -> TReturn ): ApplicationServiceRequest<TService, TReturn>
    = InMemoryApplicationServiceRequest( invoke )