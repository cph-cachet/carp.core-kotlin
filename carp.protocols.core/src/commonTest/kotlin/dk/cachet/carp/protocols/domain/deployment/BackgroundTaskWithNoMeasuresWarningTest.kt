package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.application.tasks.BackgroundTask
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


/**
 * Tests for [BackgroundTaskWithNoMeasuresWarning].
 */
class BackgroundTaskWithNoMeasuresWarningTest
{
    @Test
    fun isIssuePresent_false_for_task_with_measures()
    {
        val protocol = createEmptyProtocol()
        val taskWithMeasures = BackgroundTask( "Task", listOf( Measure.DataStream( STUB_DATA_POINT_TYPE ) ) )
        protocol.addTask( taskWithMeasures )

        val warning = BackgroundTaskWithNoMeasuresWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( emptySet(), warning.getBackgroundTasksWithNoMeasures( protocol ) )
    }

    @Test
    fun isIssuePresent_true_for_task_without_measures()
    {
        val protocol = createEmptyProtocol()
        val taskWithoutMeasures = BackgroundTask( "Task", emptyList() )
        protocol.addTask( taskWithoutMeasures )

        val warning = BackgroundTaskWithNoMeasuresWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        assertEquals( setOf( taskWithoutMeasures ), warning.getBackgroundTasksWithNoMeasures( protocol ) )
    }
}
