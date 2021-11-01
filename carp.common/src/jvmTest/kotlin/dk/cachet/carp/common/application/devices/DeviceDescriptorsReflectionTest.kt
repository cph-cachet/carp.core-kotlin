package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.commonInstances
import dk.cachet.carp.common.application.concreteDeviceDescriptorTypes
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.tasks.TaskDescriptorList
import kotlin.reflect.full.primaryConstructor
import kotlin.test.*


class DeviceDescriptorsReflectionTest
{
    private val instances = commonInstances
        .filterIsInstance<AnyDeviceDescriptor>()
        .associateBy { it::class }


    @Test
    fun all_device_descriptors_define_sensors_and_tasks() =
        concreteDeviceDescriptorTypes.forEach { descriptor ->
            val name = descriptor.simpleName
            val subclasses = descriptor.java.classes.toList()

            // Does the DeviceDescriptor list available sensors?
            val sensorsClass = subclasses.singleOrNull { it.name.endsWith( "\$Sensors" ) }
            val superSensorsClass = DataTypeSamplingSchemeMap::class
            assertNotNull( sensorsClass, "No `Sensors` subclass defined in \"$name\"." )
            assertEquals(
                sensorsClass.superclass, superSensorsClass.java,
                "`Sensors` subclass in \"$name\" does not extend from \"${superSensorsClass.simpleName}\"."
            )

            // Does the DeviceDescriptor list available tasks?
            val tasksClass = subclasses.singleOrNull { it.name.endsWith( "\$Tasks" ) }
            val superTasksClass = TaskDescriptorList::class
            assertNotNull( tasksClass, "No `Tasks` subclass defined in \"${name}\"." )
            assertEquals(
                tasksClass.superclass, superTasksClass.java,
                "`Tasks` subclass in \"$name\" does not extend from \"${superTasksClass.simpleName}\"."
            )
        }

    @Test
    fun all_device_descriptors_have_an_instance_for_testing() =
        concreteDeviceDescriptorTypes.forEach { descriptor ->
            val instance = instances[ descriptor ]
            assertNotNull( instance, "No test instance added for `$descriptor`." )
        }

    @Test
    fun all_device_descriptors_have_correct_defaults() =
        concreteDeviceDescriptorTypes.forEach { descriptor ->
            val instance = instances.getValue( descriptor )

            // Find constructor to instantiate a new instance with the default parameter values.
            // TODO: Do we always expect the primary constructor to be the one we need? Okay for now.
            val constructor = descriptor.primaryConstructor
            assertNotNull( constructor, "`$descriptor` does not have a primary constructor." )

            // For all non-optional parameters in constructor, get correct values from `instance`.
            val parameters = constructor.parameters.filter { !it.isOptional }
            val parameterValues = parameters.associateWith { parameter ->
                val matchingMember = descriptor.members.firstOrNull { it.name == parameter.name }
                assertNotNull( matchingMember, "No member with name `$parameter.name` found." )
                matchingMember.call( instance )
            }

            // Construct instance which has default values and verify whether they are correct.
            val hasDefaultValues = constructor.callBy( parameterValues )
            assertEquals(
                emptyMap(),
                hasDefaultValues.defaultSamplingConfiguration,
                "`${AnyDeviceDescriptor::defaultSamplingConfiguration.name}` of `$descriptor` doesn't have the correct default."
            )
        }
}
