package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlin.reflect.KClass


/**
 * A base [KSerializer] class to serialize instances of [Array] which allows specifying a custom [serializer] to be used to serialize the containing elements.
 *
 * @param kClass The class representation of the elements in the array.
 * @param serializer The serializer to use to serialize the elements in the array.
 */
open class CustomReferenceArraySerializer<T: Any>(
        private val kClass: KClass<T>,
        private val serializer: KSerializer<T> )
    : KSerializer<Array<T>>
{
    override val serialClassDesc: KSerialClassDesc
        get() = ArrayClassDesc

    override fun save( output: KOutput, obj: Array<T> )
    {
        val saver = ReferenceArraySerializer( kClass, serializer )
        saver.save( output, obj )
    }

    override fun load( input: KInput): Array<T>
    {
        val loader = ReferenceArraySerializer( kClass, serializer )
        return loader.load( input )
    }
}