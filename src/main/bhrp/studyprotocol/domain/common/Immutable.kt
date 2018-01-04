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
abstract class Immutable( exception: Throwable = NotImmutableError())
{
    /**
     * Exception which is thrown by default when an extending class of [Immutable] is not implemented as immutable.
     */
    class NotImmutableError: Throwable( "Immutable types may not contain mutable properties and may only contain data classes and basic types." )

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

            // Non-basic types need to be data classes to be considered immutable.
            // TODO: Is this essential? Since all properties are checked to not be mutable, this might be unnecessary.
            // TODO: However, at this point, I presume it would not make sense NOT to make them data classes.
            if ( !type.isData )
            {
                return false
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
        // Throw specified exception in case derived implementation is not immutable.
        // TODO: For performance reasons, it is probably best to only check for this during debug builds. Is this possible?
        // TODO: This could be optimized by only running it once per concrete type.
        @Suppress( "LeakingThis" ) // 'this' is only used to inspect the derived type, thus incomplete initialization is irrelevant.
        if ( !isImmutable( this::class ) )
        {
            throw exception
        }
    }
}