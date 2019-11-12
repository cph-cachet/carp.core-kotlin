package dk.cachet.carp.common.ddd

import kotlin.reflect.*


/**
 * Allows invoking a previously defined request on an application service of type [TService].
 */
interface ServiceInvoker<TService, TReturn>
{
    val function: KCallable<TReturn>

    /**
     * Invoke this request on the specified application [service].
     */
    suspend fun invokeOn( service: TService ): TReturn
}

fun <TService, TReturn> createServiceInvoker( function: KSuspendFunction1<TService, TReturn> ): ServiceInvoker<TService, TReturn>
    = object : ServiceInvoker<TService, TReturn> {
        override val function = function
        override suspend fun invokeOn( service: TService ): TReturn = function.invoke( service )
    }

fun <TService, T1, TReturn> createServiceInvoker( function: KSuspendFunction2<TService, T1, TReturn>, arg1: T1 ): ServiceInvoker<TService, TReturn>
    = object : ServiceInvoker<TService, TReturn> {
        override val function = function
        override suspend fun invokeOn( service: TService ): TReturn = function.invoke( service, arg1 )
    }

fun <TService, T1, T2, TReturn> createServiceInvoker( function: KSuspendFunction3<TService, T1, T2, TReturn>, arg1: T1, arg2: T2 ): ServiceInvoker<TService, TReturn>
    = object : ServiceInvoker<TService, TReturn> {
        override val function = function
        override suspend fun invokeOn( service: TService ): TReturn = function.invoke( service, arg1, arg2 )
    }

fun <TService, T1, T2, T3, TReturn> createServiceInvoker( function: KSuspendFunction4<TService, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 ): ServiceInvoker<TService, TReturn>
    = object : ServiceInvoker<TService, TReturn> {
        override val function = function
        override suspend fun invokeOn( service: TService ): TReturn = function.invoke( service, arg1, arg2, arg3 )
    }

fun <TService, T1, T2, T3, T4, TReturn> createServiceInvoker( function: KSuspendFunction5<TService, T1, T2, T3, T4, TReturn>, arg1: T1, arg2: T2, arg3: T3, arg4: T4 ): ServiceInvoker<TService, TReturn>
    = object : ServiceInvoker<TService, TReturn> {
        override val function = function
        override suspend fun invokeOn( service: TService ): TReturn = function.invoke( service, arg1, arg2, arg3, arg4 )
    }