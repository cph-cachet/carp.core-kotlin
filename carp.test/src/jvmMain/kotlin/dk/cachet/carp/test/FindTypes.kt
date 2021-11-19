package dk.cachet.carp.test

import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.reflect.KClass


/**
 * Find all extending types of the interface or abstract class [T].
 * It is assumed all extending classes are located in the same namespace.
 */
inline fun <reified T : Any> findConcreteTypes(): List<KClass<out T>>
{
    val klass = T::class
    check( klass.isAbstract )
    val namespace = klass.java.`package`.name

    return Reflections( namespace )
        .getSubTypesOf( klass.java )
        .filter { type ->
            // Only verify concrete types.
            !Modifier.isAbstract( type.modifiers ) && !Modifier.isInterface( type.modifiers ) &&
            // Ignore private types since they are not part of the API.
            Modifier.isPublic( type.modifiers )
        }
        .map { it.kotlin }
}
