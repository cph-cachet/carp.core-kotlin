package dk.cachet.carp.common.reflect

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
    @Suppress( "UnusedPrivateMember" ) // TODO: Remove once detekt bug is fixed: https://github.com/detekt/detekt/issues/3415
    internal inline fun <reified T> extendsType( klass: KClass<*> ): Boolean
}
