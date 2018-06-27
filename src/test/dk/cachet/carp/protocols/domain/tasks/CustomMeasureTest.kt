package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.UnknownMeasure
import dk.cachet.carp.protocols.domain.data.StubDataType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [CustomMeasure].
 */
class CustomMeasureTest
{
    @Test
    fun `initialization from json extracts base Measure properties`()
    {
        val measure = UnknownMeasure( StubDataType() )
        val serialized: String = JSON.stringify( measure )

        val custom = CustomMeasure( UnknownMeasure::class.qualifiedName!!, serialized )
        assertEquals( measure.type, custom.type )
    }

    @Serializable
    private data class IncorrectMeasure( val incorrect: String = "Not a measure." )

    @Test
    fun `initialization from invalid json fails`()
    {
        val incorrect = IncorrectMeasure()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomMeasure( IncorrectMeasure::class.qualifiedName!!, serialized )
        }
    }
}