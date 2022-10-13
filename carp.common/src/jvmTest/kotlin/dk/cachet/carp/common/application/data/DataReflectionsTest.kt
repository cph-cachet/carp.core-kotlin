package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.commonInstances
import dk.cachet.carp.common.application.concreteDataTypes
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf
import kotlin.test.*


class DataReflectionsTest
{
    private val instances = commonInstances
        .filterIsInstance<Data>()
        .associateBy { it::class }

    @Test
    fun all_data_objects_have_an_instance_for_testing() =
        concreteDataTypes.forEach { data ->
            val instance = instances[ data ]
            assertNotNull( instance, "No test instance added for `$data`." )
        }

    @Test
    fun all_sensor_data_has_correct_defaults()
    {
        val sensorDataTypes = concreteDataTypes.filter { typeOf<SensorData>() in it.supertypes }
        sensorDataTypes.forEach { sensorData ->
            val instance = instances.getValue( sensorData )

            // Find constructor to instantiate a new instance with the default parameter values.
            // TODO: Do we always expect the primary constructor to be the one we need? Okay for now.
            val constructor = sensorData.primaryConstructor
            assertNotNull( constructor, "`$sensorData` does not have a primary constructor." )

            // For all non-optional parameters in constructor, get correct values from `instance`.
            val parameters = constructor.parameters.filter { !it.isOptional }
            val parameterValues = parameters.associateWith { parameter ->
                val matchingMember = sensorData.members.firstOrNull { it.name == parameter.name }
                assertNotNull( matchingMember, "No member with name `$parameter.name` found." )
                matchingMember.call( instance )
            }

            // Construct instance which has default values and verify whether they are correct.
            val hasDefaultValues = constructor.callBy( parameterValues ) as SensorData
            assertEquals(
                null,
                hasDefaultValues.sensorSpecificData,
                "`${SensorData::sensorSpecificData.name}` of `$sensorData` doesn't have the correct default."
            )
        }
    }
}
