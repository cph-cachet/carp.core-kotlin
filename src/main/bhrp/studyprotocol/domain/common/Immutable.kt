package bhrp.studyprotocol.domain.common

import kotlin.reflect.*
import kotlin.reflect.full.*


/**
 * A base class which verifies whether the derived implementation is immutable during initialization.
 *
 * Immutable types may not contain mutable properties and may only contain data classes and basic types.
 *
 * @param exception The exception to throw in case the implementation is not immutable. [NotImmutableError] by default.
 */
@Suppress("LeakingThis") // 'this' in init is only used to inspect the derived type, thus incomplete initialization is irrelevant.
abstract class Immutable(exception: Throwable = NotImmutableError() )
{
    /**
     * Exception which is thrown by default when an extending class of [Immutable] is not implemented as immutable.
     */
    class NotImmutableError: Throwable(
        "Immutable types should be data classes, may not contain mutable properties, and may only contain basic types and other Immutable properties." )

    companion object ImmutableCheck
    {
        private val basicKotlinTypes = arrayOf(
                Double::class,
                Float::class,
                Long::class,
                Int::class,
                Short::class,
                Byte::class,
                String::class )

        private fun isImmutable( type: KClass<out Any> ): Boolean
        {
            // All basic types are immutable.
            // TODO: Verify this.
            if ( basicKotlinTypes.contains( type ) )
            {
                return true
            }

            // Containing properties which derive from Immutable are considered immutable.
            if ( type is Immutable )
            {
                return true
            }


            val properties: Iterable<KProperty1<out Any, Any?>> = type.memberProperties

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