package dk.cachet.carp.test

import kotlin.reflect.*


/**
 * Base class which can be used to implement a simple mock for the interface [TMock].
 * For now, this is very basic and solely supports tracking the last function call.
 */
abstract class Mock<TMock>
{
    private val functionCalls: MutableMap<String, Array<out Any>> = mutableMapOf()


    fun <TReturn> trackCall( function: KFunction1<TMock, TReturn> )
        = trackCallable( function )
    fun <TReturn> trackSuspendCall( function: KSuspendFunction1<TMock, TReturn> )
        = trackCallable( function )
    fun <T1, TReturn> trackCall( function: KFunction2<TMock, T1, TReturn>, arg1: T1 )
        = trackCallable( function, arg1 as Any )
    fun <T1, TReturn> trackSuspendCall( function: KSuspendFunction2<TMock, T1, TReturn>, arg1: T1 )
        = trackCallable( function, arg1 as Any )
    fun <T1, T2, TReturn> trackCall( function: KFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 )
        = trackCallable( function, arg1 as Any, arg2 as Any )
    fun <T1, T2, TReturn> trackSuspendCall( function: KSuspendFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 )
        = trackCallable( function, arg1 as Any, arg2 as Any )
    fun <T1, T2, T3, TReturn> trackCall( function: KFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 )
        = trackCallable( function, arg1 as Any, arg2 as Any, arg3 as Any )
    fun <T1, T2, T3, TReturn> trackSuspendCall( function: KSuspendFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 )
        = trackCallable( function, arg1 as Any, arg2 as Any, arg3 as Any )

    fun <TReturn> trackCallOverloaded( function: KFunction1<TMock, TReturn>, overloadIdentifier: String )
        = trackCallableOverloaded( function, overloadIdentifier )
    fun <TReturn> trackSuspendCallOverloaded( function: KSuspendFunction1<TMock, TReturn>, overloadIdentifier: String )
        = trackCallableOverloaded( function, overloadIdentifier )
    fun <T1, TReturn> trackCallOverloaded( function: KFunction2<TMock, T1, TReturn>, overloadIdentifier: String, arg1: T1 )
        = trackCallableOverloaded( function, overloadIdentifier, arg1 as Any )
    fun <T1, TReturn> trackSuspendCallOverloaded( function: KSuspendFunction2<TMock, T1, TReturn>, overloadIdentifier: String, arg1: T1 )
        = trackCallableOverloaded( function, overloadIdentifier, arg1 as Any )
    fun <T1, T2, TReturn> trackCallOverloaded( function: KFunction3<TMock, T1, T2, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2 )
        = trackCallableOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any )
    fun <T1, T2, TReturn> trackSuspendCallOverloaded( function: KSuspendFunction3<TMock, T1, T2, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2 )
        = trackCallableOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any )
    fun <T1, T2, T3, TReturn> trackCallOverloaded( function: KFunction4<TMock, T1, T2, T3, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2, arg3: T3 )
        = trackCallableOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any, arg3 as Any )
    fun <T1, T2, T3, TReturn> trackSuspendCallOverloaded( function: KSuspendFunction4<TMock, T1, T2, T3, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2, arg3: T3 )
        = trackCallableOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any, arg3 as Any )

    private fun trackCallable( function: KCallable<*>, vararg arguments: Any )
    {
        functionCalls[ function.name ] = arguments
    }
    private fun trackCallableOverloaded( function: KCallable<*>, overloadIdentifier: String, vararg arguments: Any )
    {
        functionCalls[ function.name + "-" + overloadIdentifier ] = arguments
    }


    fun <TReturn> wasCalled( function: KFunction1<TMock, TReturn> ) : Boolean
        = wasCallableCalled( function )
    fun <TReturn> wasSuspendCalled( function: KSuspendFunction1<TMock, TReturn> ) : Boolean
        = wasCallableCalled( function )
    fun <T1, TReturn> wasCalled( function: KFunction2<TMock, T1, TReturn>, arg1: T1 ): Boolean
        = wasCallableCalled( function, arg1 as Any )
    fun <T1, TReturn> wasSuspendCalled( function: KSuspendFunction2<TMock, T1, TReturn>, arg1: T1 ): Boolean
        = wasCallableCalled( function, arg1 as Any )
    fun <T1, T2, TReturn> wasCalled( function: KFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 ): Boolean
        = wasCallableCalled( function, arg1 as Any, arg2 as Any )
    fun <T1, T2, TReturn> wasSuspendCalled( function: KSuspendFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 ): Boolean
        = wasCallableCalled( function, arg1 as Any, arg2 as Any )
    fun <T1, T2, T3, TReturn> wasCalled( function: KFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 ): Boolean
        = wasCallableCalled( function, arg1 as Any, arg2 as Any, arg3 as Any )
    fun <T1, T2, T3, TReturn> wasSuspendCalled( function: KSuspendFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 ): Boolean
        = wasCallableCalled( function, arg1 as Any, arg2 as Any, arg3 as Any )

    fun <TReturn> wasCalledOverloaded( function: KFunction1<TMock, TReturn>, overloadIdentifier: String ) : Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier )
    fun <TReturn> wasSuspendCalledOverloaded( function: KSuspendFunction1<TMock, TReturn>, overloadIdentifier: String ) : Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier )
    fun <T1, TReturn> wasCalledOverloaded( function: KFunction2<TMock, T1, TReturn>, overloadIdentifier: String, arg1: T1 ): Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier, arg1 as Any )
    fun <T1, TReturn> wasSuspendCalledOverloaded( function: KSuspendFunction2<TMock, T1, TReturn>, overloadIdentifier: String, arg1: T1 ): Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier, arg1 as Any )
    fun <T1, T2, TReturn> wasCalledOverloaded( function: KFunction3<TMock, T1, T2, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2 ): Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any )
    fun <T1, T2, TReturn> wasSuspendCalledOverloaded( function: KSuspendFunction3<TMock, T1, T2, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2 ): Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any )
    fun <T1, T2, T3, TReturn> wasCalledOverloaded( function: KFunction4<TMock, T1, T2, T3, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2, arg3: T3 ): Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any, arg3 as Any )
    fun <T1, T2, T3, TReturn> wasSuspendCalledOverloaded( function: KSuspendFunction4<TMock, T1, T2, T3, TReturn>, overloadIdentifier: String, arg1: T1, arg2: T2, arg3: T3 ): Boolean
        = wasCallableCalledOverloaded( function, overloadIdentifier, arg1 as Any, arg2 as Any, arg3 as Any )

    private fun wasCallableCalled( function: KCallable<*>, vararg expectedArguments: Any ): Boolean
    {
        val calledArguments = functionCalls[ function.name ] ?: return false

        return calledArguments.contentEquals( expectedArguments )
    }
    private fun wasCallableCalledOverloaded( function: KCallable<*>, overloadIdentifier: String, vararg expectedArguments: Any ): Boolean
    {
        val calledArguments = functionCalls[ function.name + "-" + overloadIdentifier ] ?: return false

        return calledArguments.contentEquals( expectedArguments )
    }

    fun <TReturn> wasCalled( function: KCallable<TReturn>, overloadIdentifier: String? = null ) : Boolean =
        if ( overloadIdentifier == null )
            functionCalls.containsKey( function.name )
        else
            functionCalls.containsKey( function.name + "-" + overloadIdentifier )
    fun <TReturn> wasNotCalled( function: KCallable<TReturn>, overloadIdentifier: String? = null ) : Boolean
        = !wasCalled( function, overloadIdentifier )

    fun reset()
    {
        functionCalls.clear()
    }
}