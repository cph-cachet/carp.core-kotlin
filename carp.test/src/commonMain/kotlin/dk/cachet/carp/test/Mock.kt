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
        = trackCall( function.name )
    fun <TReturn> trackSuspendCall( function: KSuspendFunction1<TMock, TReturn> )
        = trackCall( function.name )
    fun <T1, TReturn> trackCall( function: KFunction2<TMock, T1, TReturn>, arg1: T1 )
        = trackCall( function.name, arg1 as Any )
    fun <T1, TReturn> trackSuspendCall( function: KSuspendFunction2<TMock, T1, TReturn>, arg1: T1 )
        = trackCall( function.name, arg1 as Any )
    fun <T1, T2, TReturn> trackCall( function: KFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 )
        = trackCall( function.name, arg1 as Any, arg2 as Any )
    fun <T1, T2, TReturn> trackSuspendCall( function: KSuspendFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 )
        = trackCall( function.name, arg1 as Any, arg2 as Any )
    fun <T1, T2, T3, TReturn> trackCall( function: KFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 )
        = trackCall( function.name, arg1 as Any, arg2 as Any, arg3 as Any )
    fun <T1, T2, T3, TReturn> trackSuspendCall( function: KSuspendFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 )
        = trackCall( function.name, arg1 as Any, arg2 as Any, arg3 as Any )
    private fun trackCall( functionName: String, vararg arguments: Any )
    {
        functionCalls[ functionName ] = arguments
    }


    fun <TReturn> wasCalled( function: KFunction1<TMock, TReturn> ) : Boolean
        = wasCalled( function.name )
    fun <TReturn> wasSuspendCalled( function: KSuspendFunction1<TMock, TReturn> ) : Boolean
        = wasCalled( function.name )
    fun <T1, TReturn> wasCalled( function: KFunction2<TMock, T1, TReturn>, arg1: T1 ): Boolean
        = wasCalled( function.name, arg1 as Any )
    fun <T1, TReturn> wasSuspendCalled( function: KSuspendFunction2<TMock, T1, TReturn>, arg1: T1 ): Boolean
        = wasCalled( function.name, arg1 as Any )
    fun <T1, T2, TReturn> wasCalled( function: KFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 ): Boolean
        = wasCalled( function.name, arg1 as Any, arg2 as Any )
    fun <T1, T2, TReturn> wasSuspendCalled( function: KSuspendFunction3<TMock, T1, T2, TReturn>, arg1: T1, arg2: T2 ): Boolean
        = wasCalled( function.name, arg1 as Any, arg2 as Any )
    fun <T1, T2, T3, TReturn> wasCalled( function: KFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 ): Boolean
        = wasCalled( function.name, arg1 as Any, arg2 as Any, arg3 as Any )
    fun <T1, T2, T3, TReturn> wasSuspendCalled( function: KSuspendFunction4<TMock, T1, T2, T3, TReturn>, arg1: T1, arg2: T2, arg3: T3 ): Boolean
        = wasCalled( function.name, arg1 as Any, arg2 as Any, arg3 as Any )
    private fun wasCalled( functionName: String, vararg expectedArguments: Any ): Boolean
    {
        val calledArguments = functionCalls[ functionName ] ?: return false

        return calledArguments.contentEquals( expectedArguments )
    }

    fun <TReturn> wasNotCalled( function: KCallable<TReturn> ) : Boolean
        = !functionCalls.containsKey( function.name )

    fun reset()
    {
        functionCalls.clear()
    }
}