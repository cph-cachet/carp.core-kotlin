package dk.cachet.carp.client.application

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*
import kotlin.test.*


/**
 * Tests for [ClientManager].
 */
class ClientManagerTest
{
    private val smartphone: Smartphone = Smartphone( "User's phone" )


    @Test
    fun add_study_succeeds()
    {
        // Create deployment and client manager.
        val ( deploymentManager, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = SmartphoneManager( smartphone.createRegistration(), deploymentManager )

        clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
    }

    @Test
    fun add_study_fails_for_invalid_deployment()
    {
        // Create deployment and client manager.
        val ( deploymentManager, _) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = SmartphoneManager( smartphone.createRegistration(), deploymentManager )

        val invalidId = UUID( "00000000-0000-0000-0000-000000000000" )
        assertFailsWith<IllegalArgumentException>
        {
            clientManager.addStudy( invalidId, smartphone.roleName )
        }
    }

    @Test
    fun add_study_fails_for_nonexisting_device_role()
    {
        // Create deployment and client manager.
        val ( deploymentManager, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = SmartphoneManager( smartphone.createRegistration(), deploymentManager )

        assertFailsWith<IllegalArgumentException>
        {
            clientManager.addStudy( deploymentStatus.studyDeploymentId, "Invalid role" )
        }
    }

    @Test
    fun add_study_fails_for_device_role_name_already_in_use()
    {
        // Create deployment and client manager.
        val ( deploymentManager, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = SmartphoneManager( smartphone.createRegistration(), deploymentManager )

        clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        assertFailsWith<IllegalArgumentException>
        {
            clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        }
    }


    /**
     * Create a study protocol with [smartphone] as the single master device, i.e., a typical 'smartphone study'.
     */
    private fun createSmartphoneStudy(): StudyProtocol
    {
        val protocol = StudyProtocol( ProtocolOwner(), "Smartphone study" )
        protocol.addMasterDevice( smartphone )
        return protocol
    }

    /**
     * Create a deployment manager which contains a study deployment for the specified [protocol].
     */
    private fun createStudyDeployment( protocol: StudyProtocol ): Pair<DeploymentManager, StudyDeploymentStatus>
    {
        val deploymentManager = DeploymentManager( InMemoryDeploymentRepository() )
        val status = deploymentManager.createStudyDeployment( protocol.getSnapshot() )
        return Pair( deploymentManager, status )
    }
}