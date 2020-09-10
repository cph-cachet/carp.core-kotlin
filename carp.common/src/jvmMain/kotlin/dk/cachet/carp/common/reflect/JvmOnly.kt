package dk.cachet.carp.common.reflect

import kotlin.reflect.KClass


actual class JvmOnly private actual constructor()
{
    actual companion object
    {
        actual val isJvm: Boolean = true
        actual inline fun <reified T> extendsType( klass: KClass<*> ): Boolean
        {
            val baseTypes = klass.supertypes.map { it.classifier as KClass<*> }
            return baseTypes.contains( T::class )
        }
    }
}
