package dk.cachet.carp.common.infrastructure.reflect

import kotlin.reflect.KCallable
import kotlin.reflect.KClass


/**
 * Provides access to reflection functionality, but only on JVM and when reflection is loaded, in common multiplatform sources.
 * Since this is not true multiplatform, this should only be used for implementation verification purposes
 * which can be executed on a JVM runtime, and thus not for functional logic required on all target platforms.
 */
fun reflectIfAvailable(): ReflectionAvailable? =
    if ( Reflection.isReflectionAvailable ) ReflectionAvailable() else null

class ReflectionAvailable internal constructor()
{
    inline fun <reified T> extendsType( klass: KClass<*> ): Boolean = Reflection.extendsType<T>( klass )

    inline fun <reified T> members(): Collection<KCallable<*>> = members( T::class )
    fun members( klass: KClass<*> ): Collection<KCallable<*>> = Reflection.members( klass )
}


@PublishedApi
internal expect object Reflection
{
    /**
     * Returns true when called from a JVM runtime environment and `kotlin.reflect` is available at runtime.
     */
    val isReflectionAvailable: Boolean

    /**
     * Determines whether [klass] extends from [T].
     */
    @PublishedApi
    internal inline fun <reified T> extendsType( klass: KClass<*> ): Boolean

    /**
     * All functions and properties accessible in this class, including those declared in this class and all of its
     * superclasses. Does not include constructors.
     */
    internal fun members( klass: KClass<*> ): Collection<KCallable<*>>
}
