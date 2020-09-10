package dk.cachet.carp.common.reflect

import kotlin.reflect.KClass


/**
 * Provides access to reflection functionality, currently only available for JVM, in common multiplatform sources.
 * Since this is not true multiplatform, this should only be used for implementation verification purposes
 * which can be executed on a JVM runtime, and thus not for functional logic required on all target platforms.
 */
expect class JvmOnly private constructor()
{
    companion object
    {
        /**
         * Returns true when called from a JVM runtime environment.
         */
        val isJvm: Boolean

        /**
         * Determines whether [klass] extends from [T].
         */
        inline fun <reified T> extendsType( klass: KClass<*> ): Boolean
    }
}
