package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass


// Serialization is not yet supported on JavaScript runtime. These annotations do nothing.
actual annotation class Serializable
actual annotation class SerializableWith actual constructor( actual val with: KClass<out KSerializer<*>> )