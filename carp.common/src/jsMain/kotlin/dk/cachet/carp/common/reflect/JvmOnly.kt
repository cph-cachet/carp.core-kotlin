package dk.cachet.carp.common.reflect

import kotlin.reflect.KClass


actual class JvmOnly private actual constructor()
{
    actual companion object
    {
        actual val isJvm: Boolean = false
        actual inline fun <reified T> extendsType( klass: KClass<*> ): Boolean =
            throw UnsupportedOperationException()
    }
}
