package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlin.reflect.KClass


/**
 * A base [KSerializer] class to serialize instances of [Array] which allows specifying a custom [serializer] to be used to serialize the containing elements.
 */
open class CustomReferenceArraySerializer<T: Any>(
    /**
     * The class representation of the elements in the array.
     */
    private val _klass: KClass<T>,
    /**
     * The serializer to use to serialize the elements in the array.
     */
    private val _serializer: KSerializer<T>
): KSerializer<Array<T>> by ReferenceArraySerializer( _klass, _serializer )