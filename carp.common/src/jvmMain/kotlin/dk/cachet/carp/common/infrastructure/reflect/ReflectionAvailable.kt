@file:Suppress(
    "MatchingDeclarationName",
    // Access to reflection here is internal, and we guarantee it is only accessed when reflection is available.
    "NO_REFLECTION_IN_CLASS_PATH"
)

package dk.cachet.carp.common.infrastructure.reflect

import kotlin.reflect.KClass


@PublishedApi
internal actual object Reflection
{
    @Suppress( "SwallowedException" )
    actual val isReflectionAvailable: Boolean =
        try
        {
            Int::class.supertypes // Accessing 'supertypes' fails if `kotlin.reflect` is not loaded.
            true
        }
        catch ( e: KotlinReflectionNotSupportedError ) { false }


    @PublishedApi
    internal actual inline fun <reified T> extendsType( klass: KClass<*> ): Boolean
    {
        val baseTypes = klass.supertypes.map { it.classifier as KClass<*> }
        return baseTypes.contains( T::class )
    }
}
