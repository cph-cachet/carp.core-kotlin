package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.serialization.createDefaultJSON
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomTaskDescriptor].
 */
class CustomTaskDescriptorTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_TaskDescriptor_properties() {
        val measures: List<Measure> = listOf( StubMeasure() )
        val task = UnknownTaskDescriptor( "Unknown", measures )
        val serialized: String = JSON.stringify( UnknownTaskDescriptor.serializer(), task )

        val custom = CustomTaskDescriptor( "Irrelevant", serialized, JSON )
        assertEquals( task.name, custom.name )
        assertEquals( task.measures.count(), task.measures.intersect( custom.measures ).count() )
    }

    @Serializable
    internal data class IncorrectTask( val incorrect: String = "Not a task." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectTask()
        val serialized: String = JSON.stringify( IncorrectTask.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTaskDescriptor( "Irrelevant", serialized, JSON )
        }
    }
}