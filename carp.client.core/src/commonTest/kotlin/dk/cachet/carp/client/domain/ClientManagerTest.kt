package dk.cachet.carp.client.domain

import dk.cachet.carp.client.infrastructure.InMemoryClientRepository
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.protocols.domain.devices.SmartphoneDeviceRegistration
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


private val unknownId = UUID( "00000000-0000-0000-0000-000000000000" )

/**
 * Tests for [ClientManager].
 */
class ClientManagerTest
{
    private suspend fun initializeSmartphoneClient( deploymentService: DeploymentService ): SmartphoneClient =
        SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataListener() ).apply {
            configure()
        }


    @Test
    fun configure_succeeds() = runBlockingTest {
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )

        // Initially not configured.
        val client = SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataListener() )
        assertFalse( client.isConfigured() )

        // Configuration succeeds.
        client.configure()
        assertTrue( client.isConfigured() )
    }

    @Test
    fun add_study_fails_when_not_yet_configured() = runBlockingTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = SmartphoneClient( InMemoryClientRepository(), deploymentService, createDataListener() )

        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        }
    }

    @Test
    fun add_study_succeeds() = runBlockingTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
    }

    @Test
    fun add_study_fails_for_invalid_deployment() = runBlockingTest {
        // Create deployment service and client manager.
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( unknownId, smartphone.roleName )
        }
    }

    @Test
    fun add_study_fails_for_nonexisting_device_role() = runBlockingTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( deploymentStatus.studyDeploymentId, "Invalid role" )
        }
    }

    @Test
    fun add_study_fails_for_study_which_was_already_added() = runBlockingTest {
        // Create deployment service and client manager.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        assertFailsWith<IllegalArgumentException>
        {
            client.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        }
    }

    @Test
    fun tryDeployment_succeeds() = runBlockingTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )
        val deploymentId = deploymentStatus.studyDeploymentId
        client.addStudy( deploymentId, smartphone.roleName )

        // Dependent device needs to be registered before the intended device can be deployed on this client.
        val dependentRegistration = SmartphoneDeviceRegistration( "dependent" )
        deploymentService.registerDevice( deploymentId, deviceSmartphoneDependsOn.roleName, dependentRegistration )

        val isDeployed = client.tryDeployment( client.getStudies().first().id )
        assertTrue( isDeployed )
    }

    @Test
    fun tryDeployment_fails_for_unknown_id() = runBlockingTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val client = initializeSmartphoneClient( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            client.tryDeployment( StudyRuntimeId( unknownId, "Unknown device role" ) )
        }
    }
}
