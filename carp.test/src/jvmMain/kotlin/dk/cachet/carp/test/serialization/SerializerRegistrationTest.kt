package dk.cachet.carp.test.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection
import kotlin.test.*


/**
 * Verifies whether all extending types of the interface or abstract class [T]
 * are registered for polymorphic serialization in [serializersModule].
 *
 * It is assumed all extending classes are located in the same namespace.
 */
@ExperimentalSerializationApi
inline fun <reified T : Any> verifyTypesAreRegistered( serializersModule: SerializersModule )
{
    val klass = T::class
    check( klass.isAbstract )
    val namespace = klass.java.`package`.name

    val polymorphicSerializers = getPolymorphicSerializers( serializersModule )

    val reflections = Reflections( namespace )
    reflections
        .getSubTypesOf( klass.java )
        .filter { serializable ->
            // Wrappers for unknown types are only used at runtime and don't need to be serializable.
            serializable.interfaces.none { it.simpleName == "UnknownPolymorphicWrapper" } &&
            // Only verify concrete types.
            !Modifier.isAbstract( serializable.modifiers ) && !Modifier.isInterface( serializable.modifiers )
        }
        .forEach {
            val kotlinClass = Reflection.createKotlinClass( it )
            val serializer = polymorphicSerializers[ kotlinClass ]
            assertNotNull( serializer, "No serializer registered for '$it'." )
        }
}
