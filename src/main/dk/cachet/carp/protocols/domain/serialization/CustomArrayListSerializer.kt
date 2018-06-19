package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.*


/**
 * A base [KSerializer] class to serialize instances of [List] which allows specifying a custom [serializer] to be used to serialize the containing elements.
 */
open class CustomArrayListSerializer<T: Any>(
    /**
     * The serializer to use to serialize the elements in the list.
     */
    private val _serializer: KSerializer<T>
): KSerializer<List<T>> by ArrayListSerializer( _serializer )