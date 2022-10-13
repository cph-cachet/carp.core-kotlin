package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.ConcreteTypesReflectionTest
import dk.cachet.carp.common.application.commonInstances
import dk.cachet.carp.common.application.concreteDataTypes
import kotlin.reflect.KClass
import kotlin.reflect.typeOf
import kotlin.test.*


private val concreteSensorDataTypes =
    concreteDataTypes
        .filter { typeOf<SensorData>() in it.supertypes }
        .map {
            @Suppress( "UNCHECKED_CAST" )
            it as KClass<out SensorData>
        }

class SensorDataReflectionsTest : ConcreteTypesReflectionTest<SensorData>(
    concreteSensorDataTypes,
    commonInstances.filterIsInstance<SensorData>()
)
{
    override fun assertValidDefaultValues( instanceWithDefaults: SensorData )
    {
        assertEquals(
            null,
            instanceWithDefaults.sensorSpecificData,
            "`${SensorData::sensorSpecificData.name}` of `$instanceWithDefaults` doesn't have the correct default."
        )
    }
}
