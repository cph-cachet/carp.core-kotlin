package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests for implementations of [ProtocolFactoryService].
 */
interface ProtocolFactoryServiceTest
{
    /**
     * Create the factory service to be used in the tests.
     */
    fun createService(): ProtocolFactoryService


    @Test
    fun createCustomProtocol_succeeds() = runTest {
        val factory = createService()

        val ownerId: UUID = UUID.randomUUID()
        val name = "Custom protocol"
        val customProtocol = "{...}"
        val description = "Description"
        val protocolSnapshot = factory.createCustomProtocol( ownerId, name, customProtocol, description )
        val protocol = StudyProtocol.fromSnapshot( protocolSnapshot )

        assertEquals( ownerId, protocol.ownerId )
        assertEquals( name, protocol.name )
        assertEquals( description, protocol.description )

        assertEquals( 1, protocol.tasks.size )
        val task = protocol.tasks.single()
        assertTrue( task is CustomProtocolTask )
        assertEquals( customProtocol, task.studyProtocol )
    }

    @Test
    fun createCustomProtocol_contains_no_deployment_issues() = runTest {
        val factory = createService()

        val protocolSnapshot = factory.createCustomProtocol( UUID.randomUUID(), "Name", "{...}" )
        val protocol = StudyProtocol.fromSnapshot( protocolSnapshot )

        assertEquals( 0, protocol.getDeploymentIssues().count() )
    }
}
