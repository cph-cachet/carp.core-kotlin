package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeList
import dk.cachet.carp.common.application.tasks.TaskDescriptorList
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class DeviceDescriptorsReflectionTest
{
    @Test
    fun all_device_descriptors_define_sensors_and_tasks()
    {
        // Find all device descriptors in this subsystem.
        val deviceClass = DeviceDescriptor::class.java
        val namespace = deviceClass.`package`.name
        val reflections = Reflections( namespace )
        val concreteDeviceDescriptors = reflections
            .getSubTypesOf( deviceClass )
            .filter { !Modifier.isAbstract( it.modifiers ) }

        concreteDeviceDescriptors.forEach { concreteClass ->
            val name = concreteClass.simpleName
            val subclasses = concreteClass.classes.toList()

            // Does the DeviceDescriptor list available sensors?
            val sensorsClass = subclasses.singleOrNull { it.name.endsWith( "\$Sensors" ) }
            val superSensorsClass = DataTypeSamplingSchemeList::class
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
}
