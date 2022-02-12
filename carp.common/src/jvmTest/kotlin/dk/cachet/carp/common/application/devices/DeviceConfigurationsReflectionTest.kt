package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.commonInstances
import dk.cachet.carp.common.application.concreteDeviceConfigurationTypes
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.tasks.TaskDescriptorList
import kotlin.reflect.full.primaryConstructor
import kotlin.test.*


class DeviceConfigurationsReflectionTest
{
    private val instances = commonInstances
        .filterIsInstance<AnyDeviceConfiguration>()
        .associateBy { it::class }


    @Test
    fun all_device_configurations_define_sensors_and_tasks() =
        concreteDeviceConfigurationTypes.forEach { configuration ->
            val name = configuration.simpleName
            val subclasses = configuration.java.classes.toList()

            // Does the DeviceConfiguration list available sensors?
            val sensorsClass = subclasses.singleOrNull { it.name.endsWith( "\$Sensors" ) }
            val superSensorsClass = DataTypeSamplingSchemeMap::class
            assertNotNull( sensorsClass, "No `Sensors` subclass defined in \"$name\"." )
            assertEquals(
                sensorsClass.superclass, superSensorsClass.java,
                "`Sensors` subclass in \"$name\" does not extend from \"${superSensorsClass.simpleName}\"."
            )

            // Does the DeviceConfiguration list available tasks?
            val tasksClass = subclasses.singleOrNull { it.name.endsWith( "\$Tasks" ) }
            val superTasksClass = TaskDescriptorList::class
            assertNotNull( tasksClass, "No `Tasks` subclass defined in \"${name}\"." )
            assertEquals(
                tasksClass.superclass, superTasksClass.java,
                "`Tasks` subclass in \"$name\" does not extend from \"${superTasksClass.simpleName}\"."
            )
        }

    @Test
    fun all_device_configurations_have_an_instance_for_testing() =
        concreteDeviceConfigurationTypes.forEach { configuration ->
            val instance = instances[ configuration ]
            assertNotNull( instance, "No test instance added for `$configuration`." )
        }

    @Test
    fun all_device_configurations_have_correct_defaults() =
        concreteDeviceConfigurationTypes.forEach { configuration ->
            val instance = instances.getValue( configuration )

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
            assertEquals(
                emptyMap(),
                hasDefaultValues.defaultSamplingConfiguration,
                "`${AnyDeviceConfiguration::defaultSamplingConfiguration.name}` of `$configuration` doesn't have the correct default."
            )
            assertEquals(
                false,
                hasDefaultValues.isOptional,
                "`${AnyDeviceConfiguration::isOptional.name}` of `$configuration` doesn't have the correct default."
            )
        }
}
