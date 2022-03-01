package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
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
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        protocol.addTask( StubTaskConfiguration() )

        val warning = UnstartedTasksWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun isIssuePresent_true_with_tasks_which_are_only_stopped()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val task = StubTaskConfiguration()
        protocol.addPrimaryDevice( device )
        protocol.addTask( task )
        protocol.addTaskControl( device.atStartOfStudy().stop( task, device ) )

        val warning = UnstartedTasksWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun getUnstartedTasks_returns_all_unstarted_tasks()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        val triggeredTask = StubTaskConfiguration( "Triggered" )
        val unstartedTask = StubTaskConfiguration( "Untriggered" )
        with ( protocol )
        {
            addPrimaryDevice( device )
            addTaskControl( StubTriggerConfiguration( device ).start( triggeredTask, device ) )
            addTask( unstartedTask )
        }

        val warning = UnstartedTasksWarning()
        val unstarted = warning.getUnstartedTasks( protocol ).toList()
        val expectedUnstarted = listOf( unstartedTask )
        assertEquals( expectedUnstarted.count(), unstarted.intersect( expectedUnstarted ).count() )
    }
}
