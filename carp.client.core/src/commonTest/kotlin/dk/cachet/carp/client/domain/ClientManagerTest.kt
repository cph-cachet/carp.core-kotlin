package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import kotlin.test.*


/**
 * Tests for [ClientManager].
 */
class ClientManagerTest
{
    @Test
    fun add_study_succeeds()
    {
        // Create deployment service and client manager.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = createSmartphoneManager( deploymentService )

        clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
    }

    @Test
    fun add_study_fails_for_invalid_deployment()
    {
        // Create deployment service and client manager.
        val ( deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = createSmartphoneManager( deploymentService )

        val invalidId = UUID( "00000000-0000-0000-0000-000000000000" )
        assertFailsWith<IllegalArgumentException>
        {
            clientManager.addStudy( invalidId, smartphone.roleName )
        }
    }

    @Test
    fun add_study_fails_for_nonexisting_device_role()
    {
        // Create deployment service and client manager.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = createSmartphoneManager( deploymentService )

        assertFailsWith<IllegalArgumentException>
        {
            clientManager.addStudy( deploymentStatus.studyDeploymentId, "Invalid role" )
        }
    }

    @Test
    fun add_study_fails_for_device_role_name_already_in_use()
    {
        // Create deployment service and client manager.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = createSmartphoneManager( deploymentService )

        clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        assertFailsWith<IllegalArgumentException>
        {
            clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )
        }
    }

    @Test
    fun creating_manager_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        // Create deployment service and client manager with one study.
        val ( deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val clientManager = createSmartphoneManager( deploymentService )
        clientManager.addStudy( deploymentStatus.studyDeploymentId, smartphone.roleName )

        val snapshot = clientManager.getSnapshot()
        val parsed = SmartphoneManager.fromSnapshot( snapshot, deploymentService ) // Optionally, this can be cast back to `SmartphoneManager`.

        assertEquals( clientManager.deviceRegistration, parsed.deviceRegistration )
        assertTrue { parsed.studies.count() == 1 } // Whether study runtime matches is tested in StudyRuntimeTest since this logic is simply delegated.
    }
}