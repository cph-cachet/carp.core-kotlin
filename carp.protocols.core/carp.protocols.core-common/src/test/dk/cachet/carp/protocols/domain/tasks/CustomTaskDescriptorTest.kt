package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.data.StubDataType
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.json.JSON
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [CustomTaskDescriptor].
 */
@JsIgnore
class CustomTaskDescriptorTest
{
    @Test
    fun initialization_from_json_extracts_base_TaskDescriptor_properties() {
        val measures: List<Measure> = listOf( StubMeasure( StubDataType() ) )
        val task = UnknownTaskDescriptor( "Unknown", measures )
        val serialized: String = JSON.stringify( task )

        val custom = CustomTaskDescriptor( "Irrelevant", serialized )
        assertEquals( task.name, custom.name )
        assertEquals( task.measures.count(), task.measures.intersect( custom.measures ).count() )
    }

    @Serializable
    private data class IncorrectTask( val incorrect: String = "Not a task." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectTask()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTaskDescriptor( "Irrelevant", serialized )
        }
    }
}