@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.tasks.getAllExpectedDataTypes
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
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
        private val JSON: Json = createDefaultJSON( STUBS_SERIAL_MODULE )
    }


    @Test
    fun initialization_from_json_extracts_base_TaskDescriptor_properties()
    {
        val measures: List<Measure> = listOf( Measure.DataStream( STUB_DATA_TYPE ) )
        val task = StubTaskDescriptor( "Unknown", measures )
        val serialized: String = JSON.encodeToString( StubTaskDescriptor.serializer(), task )

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
        val serialized: String = JSON.encodeToString( IncorrectTask.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTaskDescriptor( "Irrelevant", serialized, JSON )
        }
    }

    @Test
    fun getInteractiveDataTypes_and_getAllExpectedDataTypes_is_unsupported()
    {
        val measures: List<Measure> = listOf( Measure.DataStream( STUB_DATA_TYPE ) )
        val task = StubTaskDescriptor( "Unknown", measures )
        val serialized: String = JSON.encodeToString( StubTaskDescriptor.serializer(), task )
        val customTask = CustomTaskDescriptor( "Task", serialized, JSON )

        assertFailsWith<UnsupportedOperationException> { customTask.getInteractionDataTypes() }
        assertFailsWith<UnsupportedOperationException> { customTask.getAllExpectedDataTypes() }
    }
}
