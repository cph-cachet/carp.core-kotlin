package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.data.StubDataType
import dk.cachet.carp.protocols.domain.serialization.Serializable
import kotlinx.serialization.json.JSON
import kotlin.test.*


/**
 * Tests for [CustomMeasure].
 */
@JsIgnore
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
    private data class IncorrectMeasure( val incorrect: String = "Not a measure." )

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