package bhrp.studyprotocol.domain.deployment

import bhrp.studyprotocol.domain.createEmptyProtocol
import bhrp.studyprotocol.domain.devices.StubDeviceDescriptor
import bhrp.studyprotocol.domain.devices.StubMasterDeviceDescriptor
import bhrp.studyprotocol.domain.tasks.StubTaskDescriptor
import bhrp.studyprotocol.domain.triggers.StubTrigger
import org.junit.jupiter.api.*
import kotlin.test.*


/**
 * Tests for [UseCompositeTaskWarning].
 */
class UseCompositeTaskWarningTest
{
    @Test
    fun `isIssuePresent true when multiple tasks are sent to one device by one trigger`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        with ( protocol )
        {
            addMasterDevice( device )
            addTriggeredTask( trigger, StubTaskDescriptor( "Task 1" ), device )
            addTriggeredTask( trigger, StubTaskDescriptor( "Task 2" ), device )
        }

        val warning = UseCompositeTaskWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun `isIssuePresent false when only single tasks are triggered per device`()
    {
        val protocol = createEmptyProtocol()
        val device1 = StubMasterDeviceDescriptor()
        val device2 = StubDeviceDescriptor()
        val task = StubTaskDescriptor()
        with ( protocol )
        {
            addMasterDevice( device1 )
            addConnectedDevice( device2, device1 )
            val trigger1 = StubTrigger( device1 )
            addTriggeredTask( trigger1, task, device1 )
            addTriggeredTask( trigger1, task, device2 )
            addTriggeredTask( StubTrigger( device2 ), task, device1 )
        }

        val warning = UseCompositeTaskWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun `getOverlappingTasks returns all overlapping tasks`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val task1 = StubTaskDescriptor( "Task 1" )
        val task2 = StubTaskDescriptor( "Task 2" )
        with ( protocol )
        {
            protocol.addMasterDevice( device )
            protocol.addTriggeredTask( trigger, task1, device )
            protocol.addTriggeredTask( trigger, task2, device )
        }

        val warning = UseCompositeTaskWarning()
        val overlapping = warning.getOverlappingTasks( protocol )
        val expectedOverlapping = listOf( UseCompositeTaskWarning.OverlappingTasks( trigger, device, listOf( task1, task2 ) ) )
        assertEquals( expectedOverlapping.count(), overlapping.intersect( expectedOverlapping ).count() )
    }
}