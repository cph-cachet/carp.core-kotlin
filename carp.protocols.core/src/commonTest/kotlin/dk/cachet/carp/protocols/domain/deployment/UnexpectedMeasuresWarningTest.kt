package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


class UnexpectedMeasuresWarningTest
{
    @Test
    fun isIssuePresent_true_when_unexpected_measure_in_protocol()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration() // Supports STUB_DATA_TYPE.
        protocol.addPrimaryDevice( primary )
        val expectedMeasure = Measure.DataStream( STUB_DATA_POINT_TYPE )
        val unexpectedMeasure = Measure.DataStream( DataType( "namespace", "unexpected" ) )
        val task = StubTaskConfiguration( "Task", listOf( expectedMeasure, unexpectedMeasure ) )
        protocol.addTaskControl( primary.atStartOfStudy().start( task, primary ) )

        val warning = UnexpectedMeasuresWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val unexpectedMeasures = warning.getUnexpectedMeasures( protocol )

        assertEquals(
            UnexpectedMeasuresWarning.UnexpectedMeasure( primary, unexpectedMeasure ),
            unexpectedMeasures.single()
        )
    }

    @Test
    fun isIssuePresent_false_when_no_unexpected_measures_in_protocol()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration() // Supports STUB_DATA_TYPE.
        protocol.addPrimaryDevice( primary )
        val measure = Measure.DataStream( STUB_DATA_POINT_TYPE )
        val task = StubTaskConfiguration( "Task", listOf( measure ) )
        protocol.addTaskControl( primary.atStartOfStudy().start( task, primary ) )

        val warning = UnexpectedMeasuresWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        val unexpectedMeasures = warning.getUnexpectedMeasures( protocol )

        assertTrue( unexpectedMeasures.isEmpty() )
    }
}
