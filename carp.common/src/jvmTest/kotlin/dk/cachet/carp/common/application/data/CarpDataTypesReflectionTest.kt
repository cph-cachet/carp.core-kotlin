package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.concreteDataTypes
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.CustomInput
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.test.*


class CarpDataTypesReflectionTest
{
    @OptIn( InternalSerializationApi::class, ExperimentalSerializationApi::class )
    @Test
    fun all_data_types_included()
    {
        val inputDataTypes = CarpInputDataTypes.map { it.toString() }.toSet()
        val dataTypes = concreteDataTypes
            .filter {
                it != NoData::class && // Generic type placeholder which shouldn't be included in `CarpDataTypes`.
                it != CustomInput::class // Exceptional input type which shouldn't be included in `CarpInputDataTypes`.
            }
            .map { it.serializer().descriptor.serialName }
            .minus( inputDataTypes )

        dataTypes.forEach {
            val dataType = DataType.fromString( it )
            assertTrue(
                dataType in CarpDataTypes,
                "Data type \"$it\" isn't registered in ${CarpDataTypes::class.simpleName}."
            )
        }
    }
}
