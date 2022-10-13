package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.test.findConcreteTypes
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.test.*


val concreteDataTypes: List<KClass<out Data>> = findConcreteTypes()
val concreteInputElementTypes: List<KClass<out InputElement<*>>> = findConcreteTypes()
val concreteDeviceConfigurationTypes: List<KClass<out AnyDeviceConfiguration>> = findConcreteTypes()
val concreteDeviceRegistrationTypes: List<KClass<out DeviceRegistration>> = findConcreteTypes()
val concreteSamplingConfigurationTypes: List<KClass<out SamplingConfiguration>> = findConcreteTypes()
val concreteTaskConfigurationTypes: List<KClass<out TaskConfiguration<*>>> = findConcreteTypes()
val concreteTriggerConfigurationTypes: List<KClass<out TriggerConfiguration<*>>> = findConcreteTypes()
val concreteAccountIdentityTypes: List<KClass<out AccountIdentity>> = findConcreteTypes()


/**
 * Base test class to help verify whether concrete types of [T] are implemented correctly.
 */
abstract class ConcreteTypesReflectionTest<T : Any>(
    private val instanceTypes: List<KClass<out T>>,
    instances: List<T>
)
{
    private val instanceByType: Map<KClass<out T>, T> = instances.associateBy { it::class }

    init
    {
        instanceTypes.forEach { type ->
            val instance = instanceByType[ type ]
            requireNotNull( instance ) { "No test instance added for `$type`." }
        }
    }


    @Test
    fun all_concrete_types_have_correct_defaults() =
        instanceTypes.forEach { configuration ->
            val instance = instanceByType.getValue( configuration )

            // Find constructor to instantiate a new instance with the default parameter values.
            // TODO: Do we always expect the primary constructor to be the one we need? Okay for now.
            val constructor = configuration.primaryConstructor
            assertNotNull( constructor, "`$configuration` does not have a primary constructor." )

            // For all non-optional parameters in constructor, get correct values from `instance`.
            val parameters = constructor.parameters.filter { !it.isOptional }
            val parameterValues = parameters.associateWith { parameter ->
                val matchingMember = configuration.members.firstOrNull { it.name == parameter.name }
                assertNotNull( matchingMember, "No member with name `$parameter.name` found." )
                matchingMember.call( instance )
            }

            // Construct instance which has default values and verify whether they are correct.
            val hasDefaultValues = constructor.callBy( parameterValues )
            assertValidDefaultValues( hasDefaultValues )
        }

    abstract fun assertValidDefaultValues( instanceWithDefaults: T )
}
