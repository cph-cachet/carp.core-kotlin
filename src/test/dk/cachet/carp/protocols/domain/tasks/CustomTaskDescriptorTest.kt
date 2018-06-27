package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.data.StubDataType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [CustomTaskDescriptor].
 */
class CustomTaskDescriptorTest
{
    @Test
    fun `initialization from json extracts base TaskDescriptor properties`() {
        val measures: List<Measure> = listOf( StubMeasure( StubDataType() ) )
        val task = UnknownTaskDescriptor( "Unknown", measures )
        val serialized: String = JSON.stringify( task )

        val custom = CustomTaskDescriptor( UnknownTaskDescriptor::class.qualifiedName!!, serialized )
        assertEquals( task.name, custom.name )
        assertEquals( task.measures.count(), task.measures.intersect( custom.measures ).count() )
    }

    @Serializable
    private data class IncorrectTask( val incorrect: String = "Not a task." )

    @Test
    fun `initialization from invalid json fails`()
    {
        val incorrect = IncorrectTask()
        val serialized: String = JSON.stringify( incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTaskDescriptor( IncorrectTask::class.qualifiedName!!, serialized )
        }
    }
}