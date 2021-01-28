@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.reflect

import kotlin.reflect.KClass


@PublishedApi
internal actual object Reflection
{
    actual val isReflectionAvailable: Boolean = false

    @PublishedApi
    @Suppress( "UnusedPrivateMember" ) // TODO: Remove once detekt bug is fixed: https://github.com/detekt/detekt/issues/3415
    internal actual inline fun <reified T> extendsType( klass: KClass<*> ): Boolean =
        throw UnsupportedOperationException()
}
