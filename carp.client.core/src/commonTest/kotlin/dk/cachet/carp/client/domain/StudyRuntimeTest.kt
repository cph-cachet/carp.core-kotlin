package dk.cachet.carp.client.domain

import dk.cachet.carp.client.infrastructure.*
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistrationBuilder
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests for [StudyRuntime].
 */
class StudyRuntimeTest
{
    @Test
    fun creating_runtime_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        // Create deployment with one 'smartphone' study.
        val ( deploymentManager, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = DefaultDeviceRegistrationBuilder().build()
        val runtime = StudyRuntime.initialize( deploymentManager, deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val snapshot = StudyRuntimeSnapshot.fromStudyRuntime( runtime )

        val serialized = snapshot.toJson()
        val parsed = StudyRuntimeSnapshot.fromJson( serialized )

        assertEquals( runtime.studyDeploymentId, parsed.studyDeploymentId )
        assertEquals( runtime.deviceRoleName, parsed.deviceRoleName )
    }
}