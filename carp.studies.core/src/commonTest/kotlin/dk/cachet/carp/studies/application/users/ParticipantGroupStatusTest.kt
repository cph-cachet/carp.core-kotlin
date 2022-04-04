package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantStatus
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [ParticipantGroupStatus].
 */
class ParticipantGroupStatusTest
{
    private val now = Clock.System.now()
    private val deploymentId = UUID.randomUUID()
    private val deviceStatusList = emptyList<DeviceDeploymentStatus>()
    private val participants: Set<Participant> = emptySet()
    private val participantStatusList: List<ParticipantStatus> = emptyList()


    @Test
    fun fromDeploymentStatus_returns_Invited_while_deploying_devices()
    {
        val deployingDevices = StudyDeploymentStatus.DeployingDevices( now, deploymentId, deviceStatusList, participantStatusList, null )

        val status = ParticipantGroupStatus.InDeployment.fromDeploymentStatus( participants, deployingDevices )
        assertTrue( status is ParticipantGroupStatus.Invited )
    }

    @Test
    fun fromDeploymentStatus_returns_Running_even_when_reregistering_devices()
    {
        val startedOn = Clock.System.now()
        val redeployingDevices = StudyDeploymentStatus.DeployingDevices( now, deploymentId, deviceStatusList, participantStatusList, startedOn )

        val status = ParticipantGroupStatus.InDeployment.fromDeploymentStatus( participants, redeployingDevices )
        assertTrue( status is ParticipantGroupStatus.Running )
    }
}
