package dk.cachet.carp.common.data

import dk.cachet.carp.common.serialization.COMMON_SERIAL_MODULE
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.*


/**
 * Tests for [CarpDataTypes].
 */
class CarpDataTypesTest
{
    @ExperimentalSerializationApi
    @Test
    fun serializable_data_class_registered_for_each_data_type()
    {
        CarpDataTypes.forEach {
            val serializer = COMMON_SERIAL_MODULE.getPolymorphic( Data::class, "${it.namespace}.${it.name}" )
            assertNotNull( serializer )
        }
    }
}
