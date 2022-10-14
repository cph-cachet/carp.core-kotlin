package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.ConcreteTypesReflectionTest
import dk.cachet.carp.common.application.commonInstances
import dk.cachet.carp.common.application.concreteDeviceConfigurationTypes
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.tasks.TaskConfigurationList
import kotlin.test.*


class DeviceConfigurationsReflectionTest : ConcreteTypesReflectionTest<AnyDeviceConfiguration>(
    concreteDeviceConfigurationTypes,
    commonInstances.filterIsInstance<AnyDeviceConfiguration>()
)
{
    override fun assertValidDefaultValues( instanceWithDefaults: AnyDeviceConfiguration )
    {
        assertEquals(
            emptyMap(),
            instanceWithDefaults.defaultSamplingConfiguration,
            "`${AnyDeviceConfiguration::defaultSamplingConfiguration.name}` of `$instanceWithDefaults` doesn't have the correct default."
        )
        assertEquals(
            false,
            instanceWithDefaults.isOptional,
            "`${AnyDeviceConfiguration::isOptional.name}` of `$instanceWithDefaults` doesn't have the correct default."
        )
    }

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
            val superTasksClass = TaskConfigurationList::class
            assertNotNull( tasksClass, "No `Tasks` subclass defined in \"${name}\"." )
            assertEquals(
                tasksClass.superclass, superTasksClass.java,
                "`Tasks` subclass in \"$name\" does not extend from \"${superTasksClass.simpleName}\"."
            )
        }
}
