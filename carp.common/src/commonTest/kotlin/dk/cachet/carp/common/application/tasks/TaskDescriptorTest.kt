package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import kotlin.test.*


/**
 * Tests for [TaskDescriptor].
 */
class TaskDescriptorTest
{
    @Test
    fun getInteractionDataTypes_succeeds()
    {
        val interactionType = DataType( "some.namespace", "completedtask" )
        val task = StubTaskDescriptor( "Task", emptyList(), "Description", setOf( interactionType ) )

        val dataTypes = task.getInteractionDataTypes()
        assertEquals( setOf( interactionType ), dataTypes )
    }

    @Test
    fun getAllExpectedDataTypes_succeeds()
    {
        val interactionType = DataType( "some.namespace", "completedtask" )
        val task = StubTaskDescriptor(
            "Task",
            listOf( Measure.DataStream( STUB_DATA_TYPE ), Measure.TriggerData( 0 ) ),
            "Description",
            setOf( interactionType )
        )

        val dataTypes = task.getAllExpectedDataTypes()
        assertEquals(
            setOf( STUB_DATA_TYPE, CarpDataTypes.TRIGGERED_TASK.type, interactionType ),
            dataTypes
        )
    }
}
