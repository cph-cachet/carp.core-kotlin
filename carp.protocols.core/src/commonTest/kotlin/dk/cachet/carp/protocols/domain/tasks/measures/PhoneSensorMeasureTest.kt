package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.common.data.DataType
import kotlin.test.*


/**
 * Tests for [PhoneSensorMeasure].
 */
class PhoneSensorMeasureTest
{
    @Test
    fun initialization_with_unsupported_data_type_fails()
    {
        val invalidType = DataType( "not", "supported" )
        val validMeasure = PhoneSensorMeasure.geolocation()

        assertFailsWith<IllegalArgumentException>
        {
            // It is very unlikely somebody would try to initialize the measure like this, but the exception prevents it.
            validMeasure.copy( type = invalidType )
        }
    }
}
