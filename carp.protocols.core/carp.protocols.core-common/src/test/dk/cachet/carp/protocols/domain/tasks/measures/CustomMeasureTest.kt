package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.data.StubDataType
import kotlinx.serialization.json.JSON
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [CustomMeasure].
 */
class CustomMeasureTest
{
    @Test
    fun initialization_from_json_extracts_base_Measure_properties()
    {
        val measure = UnknownMeasure( StubDataType() )
        val serialized: String = JSON.stringify( measure )

        val custom = CustomMeasure( "Irrelevant", serialized )
        assertEquals( measure.type, custom.type )
    }

    @Serializable
    internal data class IncorrectMeasure( val incorrect: String = "Not a measure." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectMeasure()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomMeasure( "Irrelevant", serialized )
        }
    }
}