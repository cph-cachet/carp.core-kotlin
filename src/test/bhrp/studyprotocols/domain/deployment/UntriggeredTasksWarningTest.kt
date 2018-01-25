package bhrp.studyprotocols.domain.deployment

import bhrp.studyprotocols.domain.createEmptyProtocol
import bhrp.studyprotocols.domain.devices.*
import bhrp.studyprotocols.domain.tasks.*
import bhrp.studyprotocols.domain.triggers.*
import org.junit.jupiter.api.Test
import org.junit.Assert.*


/**
 * Tests for [UntriggeredTasksWarning].
 */
class UntriggeredTasksWarningTest
{
    @Test
    fun `isIssuePresent false with no triggers`()
    {
        val protocol = createEmptyProtocol()

        val warning = UntriggeredTasksWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun `isIssuePresent true with untriggered tasks`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTask( StubTaskDescriptor() )

        val warning = UntriggeredTasksWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun `getUntriggeredTasks returns all untriggered tasks`()
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

        var warning = UntriggeredTasksWarning()
        val untriggered = warning.getUntriggeredTasks( protocol ).toList()
        val expectedUntriggered = listOf( untriggeredTask )
        assertEquals( expectedUntriggered.count(), untriggered.intersect( expectedUntriggered ).count() )
    }
}