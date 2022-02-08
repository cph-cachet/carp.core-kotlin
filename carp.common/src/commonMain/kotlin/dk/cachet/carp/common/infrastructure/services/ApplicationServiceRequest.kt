package dk.cachet.carp.common.infrastructure.services

import kotlinx.serialization.KSerializer


/**
 * A request for [TService] stored in memory, which can be invoked using [invokeOn].
 */
interface ApplicationServiceRequest<TService, out TReturn>
{
    fun getResponseSerializer(): KSerializer<out TReturn>
    suspend fun invokeOn( service: TService ): TReturn
}
