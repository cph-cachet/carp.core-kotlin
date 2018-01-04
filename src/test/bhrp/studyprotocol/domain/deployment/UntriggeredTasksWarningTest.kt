package bhrp.studyprotocol.domain.deployment

import bhrp.studyprotocol.domain.createEmptyProtocol
import bhrp.studyprotocol.domain.tasks.StubTaskDescriptor
import org.junit.jupiter.api.*
import kotlin.test.*


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
        fail( "Triggers are not implemented yet." )
    }

    @Test
    fun `getUntriggeredTasks returns all untriggered tasks`()
    {
        var warning = UntriggeredTasksWarning()
        val protocol = createEmptyProtocol()
        val untriggeredTask = StubTaskDescriptor( "Untriggered" )
        val triggeredTask = StubTaskDescriptor( "Triggered" )
        with ( protocol )
        {
            addTask( untriggeredTask )
            addTask( triggeredTask )

            // TODO: Hook up triggeredTask to a trigger.
        }

        val untriggered = warning.getUntriggeredTasks().toList()
        val expectedUntriggered = listOf( untriggeredTask )
        assertEquals( expectedUntriggered.count(), untriggered.intersect( expectedUntriggered ).count() )
    }
}