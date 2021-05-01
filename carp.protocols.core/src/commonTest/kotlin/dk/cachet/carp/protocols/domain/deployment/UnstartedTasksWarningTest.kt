package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTrigger
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.domain.stop
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*

/**
 * Tests for [UnstartedTasksWarning].
 */
class UnstartedTasksWarningTest
{
    @Test
    fun isIssuePresent_false_with_no_triggers()
    {
        val protocol = createEmptyProtocol()

        val warning = UnstartedTasksWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun isIssuePresent_true_with_tasks_with_no_associated_triggers()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTask( StubTaskDescriptor() )

        val warning = UnstartedTasksWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun isIssuePresent_true_with_tasks_which_are_only_stopped()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val task = StubTaskDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTask( task )
        protocol.addTaskControl( device.atStartOfStudy().stop( task, device ) )

        val warning = UnstartedTasksWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun getUnstartedTasks_returns_all_unstarted_tasks()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val triggeredTask = StubTaskDescriptor( "Triggered" )
        val unstartedTask = StubTaskDescriptor( "Untriggered" )
        with ( protocol )
        {
            addMasterDevice( device )
            addTaskControl( StubTrigger( device ).start( triggeredTask, device ) )
            addTask( unstartedTask )
        }

        val warning = UnstartedTasksWarning()
        val unstarted = warning.getUnstartedTasks( protocol ).toList()
        val expectedUnstarted = listOf( unstartedTask )
        assertEquals( expectedUnstarted.count(), unstarted.intersect( expectedUnstarted ).count() )
    }
}
