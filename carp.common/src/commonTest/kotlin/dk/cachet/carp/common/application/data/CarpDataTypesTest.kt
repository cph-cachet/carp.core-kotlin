package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.infrastructure.serialization.COMMON_SERIAL_MODULE
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.*


/**
 * Tests for [CarpDataTypes].
 */
class CarpDataTypesTest
{
    @Test
    fun data_types_gets_added_to_underlying_list()
    {
        val size = CarpDataTypes.size
        assertTrue( size > 0 )
    }

    @ExperimentalSerializationApi
    @Test
    fun serializable_data_class_registered_for_each_data_type()
    {
        CarpDataTypes.forEach {
            val serializer = COMMON_SERIAL_MODULE.getPolymorphic( Data::class, it.key.toString() )
            assertNotNull( serializer )
        }
    }
}
