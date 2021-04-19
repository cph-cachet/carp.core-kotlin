@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.infrastructure.reflect

import kotlin.reflect.KCallable
import kotlin.reflect.KClass


@PublishedApi
@Suppress( "UnusedPrivateMember" ) // TODO: Remove once detekt bug is fixed: https://github.com/detekt/detekt/issues/3415
internal actual object Reflection
{
    actual val isReflectionAvailable: Boolean = false

    @PublishedApi
    internal actual inline fun <reified T> extendsType( klass: KClass<*> ): Boolean =
        throw UnsupportedOperationException()

    internal actual fun members( klass: KClass<*> ): Collection<KCallable<*>> =
        throw UnsupportedOperationException()
}
