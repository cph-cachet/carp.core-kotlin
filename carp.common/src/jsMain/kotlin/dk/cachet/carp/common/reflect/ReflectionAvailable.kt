@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.reflect

import kotlin.reflect.KClass


@PublishedApi
internal actual object Reflection
{
    actual val isReflectionAvailable: Boolean = false

    @PublishedApi
    internal actual inline fun <reified T> extendsType( klass: KClass<*> ): Boolean =
        throw UnsupportedOperationException()
}
