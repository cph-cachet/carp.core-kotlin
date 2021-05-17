package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.infrastructure.serialization.COMMON_SERIAL_MODULE
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
            val serializer = COMMON_SERIAL_MODULE.getPolymorphic( Data::class, it.toString() )
            assertNotNull( serializer )
        }
    }
}
