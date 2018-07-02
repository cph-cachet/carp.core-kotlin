package dk.cachet.carp.protocols.domain.common

import kotlinx.serialization.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError


/**
 * A base class which verifies whether the derived implementation is immutable during initialization.
 *
 * Immutable types may not contain mutable properties and may only contain data classes and basic types.
 *
 * @param exception The exception to throw in case the implementation is not immutable. [NotImmutableError] by default.
 */
@Suppress("LeakingThis") // 'this' in init is only used to inspect the derived type, thus incomplete initialization is irrelevant.
@Serializable
abstract class Immutable(
    // To make serialization work, exception needs to be a member variable and can not just be passed as a constructor parameter.
    // However, annotating it as @Transient excludes it from serialization.
    @Transient
    private val exception: Throwable = NotImmutableError() )
{
    /**
     * Exception which is thrown by default when an extending class of [Immutable] is not implemented as immutable.
     */
    class NotImmutableError(
        error: String = "Immutable types should be data classes, may not contain mutable properties, and may only contain basic types and other Immutable properties." ) : Throwable( error )

    companion object ImmutableCheck
    {
        private val basicKotlinTypes = arrayOf(
            Double::class,
            Float::class,
            Long::class,
            Int::class,
            Short::class,
            Byte::class,
            String::class,
            Throwable::class )

        private fun isImmutable( type: KClass<out Any> ): Boolean
        {
            // The following basic types are immutable.
            // TODO: Verify this.
            if ( basicKotlinTypes.contains( type ) )
            {
                return true
            }

            // Kotlin's immutable collections are immutable.
            // TODO: Add other immutable collections.
            // TODO: Check whether type parameters are immutable. This is not possible at runtime, except maybe when using inline functions:
            // https://stackoverflow.com/questions/43184854/how-to-get-generic-param-class-in-kotlin
            if ( type.qualifiedName == "kotlin.collections.List" )
            {
                return true
            }

            // For now, assume enum's are immutable.
            // TODO: Check for vars in enums (currently not possible). This is extremely uncommon, so ignore for now.
            //       Apparently, var's can be added to enums and modified through functions.
            val isEnum = type.supertypes.any { t -> (t.classifier as KClass<out Any>).qualifiedName == "kotlin.Enum" }
            if ( isEnum )
            {
                return true
            }

            // Containing properties which derive from Immutable are considered immutable.
            if ( type is Immutable )
            {
                return true
            }

            // Get properties.
            val properties: Iterable<KProperty1<out Any, Any?>>
            try
            {
                properties = type.memberProperties
            }
            catch ( e: KotlinReflectionInternalError )
            {
                throw NotImmutableError( "'$type' can currently not be verified by '${Immutable::class.simpleName}'." )
            }

            // None of the properties should be mutable.
            if  ( properties.filter { it is KMutableProperty<*> }.any() )
            {
                return false
            }

            // Recursively, check whether all members within the containing properties are immutable.
            return properties.all { isImmutable( it.returnType.classifier as KClass<out Any> ) }
        }
    }


    init
    {
        // Immutable types need to be data classes. It does not make sense NOT to make them data classes.
        if ( !this::class.isData )
        {
            throw exception
        }

        // Implementation may not contain any mutable properties.
        // TODO: For performance reasons, it is probably best to only check for this during debug builds. Is this possible?
        // TODO: This could be optimized by only running it once per concrete type.
        if ( !isImmutable( this::class ) )
        {
            throw exception
        }
    }
}