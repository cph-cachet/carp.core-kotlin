package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTrigger
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*

/**
 * Tests for [UntriggeredTasksWarning].
 */
class UntriggeredTasksWarningTest
{
    @Test
    fun isIssuePresent_false_with_no_triggers()
    {
        val protocol = createEmptyProtocol()

        val warning = UntriggeredTasksWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun isIssuePresent_true_with_untriggered_tasks()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTask( StubTaskDescriptor() )

        val warning = UntriggeredTasksWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun getUntriggeredTasks_returns_all_untriggered_tasks()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val triggeredTask = StubTaskDescriptor( "Triggered" )
        val untriggeredTask = StubTaskDescriptor( "Untriggered" )
        with ( protocol )
        {
            addMasterDevice( device )
            addTriggeredTask( StubTrigger( device ), triggeredTask, device )
            addTask( untriggeredTask )
        }

        val warning = UntriggeredTasksWarning()
        val untriggered = warning.getUntriggeredTasks( protocol ).toList()
        val expectedUntriggered = listOf( untriggeredTask )
        assertEquals( expectedUntriggered.count(), untriggered.intersect( expectedUntriggered ).count() )
    }
}
