package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.application.users.AccountIdentity
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.reflect.KClass


val concreteDataTypes: List<KClass<out Data>> = findConcreteTypes()
val concreteInputElementTypes: List<KClass<out InputElement<*>>> = findConcreteTypes()
val concreteDeviceDescriptorTypes: List<KClass<out AnyDeviceDescriptor>> = findConcreteTypes()
val concreteDeviceRegistrationTypes: List<KClass<out DeviceRegistration>> = findConcreteTypes()
val concreteSamplingConfigurationTypes: List<KClass<out SamplingConfiguration>> = findConcreteTypes()
val concreteTaskDescriptorTypes: List<KClass<out TaskDescriptor>> = findConcreteTypes()
val concreteTriggerTypes: List<KClass<out Trigger<*>>> = findConcreteTypes()
val concreteAccountIdentityTypes: List<KClass<out AccountIdentity>> = findConcreteTypes()


/**
 * Find all extending types of the interface or abstract class [T].
 * It is assumed all extending classes are located in the same namespace.
 */
private inline fun <reified T : Any> findConcreteTypes(): List<KClass<out T>>
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
