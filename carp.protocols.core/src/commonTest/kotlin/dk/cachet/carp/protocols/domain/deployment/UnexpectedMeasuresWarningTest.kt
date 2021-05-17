package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


class UnexpectedMeasuresWarningTest
{
    @Test
    fun isIssuePresent_true_when_unexpected_measure_in_protocol()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor() // Supports STUB_DATA_TYPE.
        protocol.addMasterDevice( master )
        val expectedMeasure = Measure( STUB_DATA_TYPE )
        val unexpectedMeasure = Measure( DataType( "namespace", "unexpected" ) )
        val task = StubTaskDescriptor( "Task", listOf( expectedMeasure, unexpectedMeasure ) )
        protocol.addTaskControl( master.atStartOfStudy().start( task, master ) )

        val warning = UnexpectedMeasuresWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val unexpectedMeasures = warning.getUnexpectedMeasures( protocol )

        assertEquals(
            UnexpectedMeasuresWarning.UnexpectedMeasure( master, unexpectedMeasure ),
            unexpectedMeasures.single()
        )
    }

    @Test
    fun isIssuePresent_false_when_no_unexpected_measures_in_protocol()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor() // Supports STUB_DATA_TYPE.
        protocol.addMasterDevice( master )
        val measure = Measure( STUB_DATA_TYPE )
        val task = StubTaskDescriptor( "Task", listOf( measure ) )
        protocol.addTaskControl( master.atStartOfStudy().start( task, master ) )

        val warning = UnexpectedMeasuresWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        val unexpectedMeasures = warning.getUnexpectedMeasures( protocol )

        assertTrue( unexpectedMeasures.isEmpty() )
    }
}
