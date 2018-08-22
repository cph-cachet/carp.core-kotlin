package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass


/**
 * The 'kotlinx.serialization' library does not yet support polymorphic serialization in JavaScript.
 * By mapping [Serializable] to a dummy annotation in JavaScript and make it a type alias in the JVM to kotlinx `Serializable`,
 * serialization is supported in the JVM runtime without breaking JS compilation.
 */
@Target( AnnotationTarget.PROPERTY, AnnotationTarget.CLASS )
expect annotation class Serializable()

/**
 * The 'kotlinx.serialization' library does not yet support polymorphic serialization in JavaScript.
 * By mapping [SerializableWith] to a dummy annotation in JavaScript and make it a type alias in the JVM to kotlinx `Serializable`,
 * serialization is supported in the JVM runtime without breaking JS compilation.
 */
@Target( AnnotationTarget.PROPERTY, AnnotationTarget.CLASS )
expect annotation class SerializableWith( val with: KClass<out KSerializer<*>> )