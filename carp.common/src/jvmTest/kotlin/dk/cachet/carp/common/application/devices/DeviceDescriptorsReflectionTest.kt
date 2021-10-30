package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.concreteDeviceDescriptorTypes
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.tasks.TaskDescriptorList
import kotlin.test.*


class DeviceDescriptorsReflectionTest
{
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
}
