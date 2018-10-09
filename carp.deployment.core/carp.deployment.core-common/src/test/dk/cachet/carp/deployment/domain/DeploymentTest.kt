package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.*
import kotlin.test.*


/**
 * Tests for [Deployment].
 */
class DeploymentTest
{
    @Test
    fun creating_deployment_fromStatus_obtained_by_getStatus_is_the_same()
    {
        val owner = ProtocolOwner( UUID( "f3f4d91b-56b5-4117-bb98-7e2923cb2223" ) )
        val protocol = StudyProtocol( owner, "Test" )
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        val deployment = Deployment( snapshot, UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" ) )

        val status: DeploymentStatus = deployment.getStatus()
        val fromStatus = Deployment.fromStatus( snapshot, status )

        assertEquals( status.deploymentId, fromStatus.id )
    }
}