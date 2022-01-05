package dk.cachet.carp.clients.domain.study

import dk.cachet.carp.clients.application.study.StudyStatus
import dk.cachet.carp.clients.domain.connectedDevice
import dk.cachet.carp.clients.domain.smartphone
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.test.runSuspendTest
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [Study].
 *
 * TODO: This solely tests happy paths state transitions; we need to test/fail on unexpected calls.
 */
class StudyTest
{
    private val deploymentId: UUID = UUID.randomUUID()
    private val device: AnyMasterDeviceDescriptor = StubMasterDeviceDescriptor( "Device role" )
    private val dependentDevice: AnyMasterDeviceDescriptor = StubMasterDeviceDescriptor( "Other device" )

    private fun deploymentNotStarted(
        device: AnyMasterDeviceDescriptor,
        dependentDevice: AnyMasterDeviceDescriptor? = null
    ) = Pair(
        Study( deploymentId, device.roleName ),
        if (dependentDevice != null ) twoDeviceDeployment( device, dependentDevice )
        else singleDeviceDeployment( device )
    )

    private fun awaitingOtherDeviceRegistrations(
        device: AnyMasterDeviceDescriptor,
        dependentDevice: AnyMasterDeviceDescriptor
    ) =
        deploymentNotStarted( device, dependentDevice ).also { (study, deployment) ->
            deployment.registerDevice( device, device.createRegistration() )
            study.deploymentStatusReceived( deployment.getStatus() )
            study.consumeEvents()
        }

    private fun awaitingDeviceDeployment(
        device: AnyMasterDeviceDescriptor,
        dependentDevice: AnyMasterDeviceDescriptor? = null
    ) =
        deploymentNotStarted( device, dependentDevice ).also { (study, deployment) ->
            deployment.registerDevice( device, device.createRegistration() )
            if ( dependentDevice != null )
            {
                deployment.registerDevice( dependentDevice, dependentDevice.createRegistration() )
            }
            study.deploymentStatusReceived( deployment.getStatus() )
            study.consumeEvents()
        }

    private fun registeringDevices(
        device: AnyMasterDeviceDescriptor,
        dependentDevice: AnyMasterDeviceDescriptor? = null
    ) =
        awaitingDeviceDeployment( device, dependentDevice ).also { (study, deployment) ->
            val deviceDeployment = deployment.getDeviceDeploymentFor( device )
            study.deviceDeploymentReceived( deviceDeployment )
            study.consumeEvents()
        }

    private fun awaitingOtherDeviceDeployments(
        device: AnyMasterDeviceDescriptor,
        dependentDevice: AnyMasterDeviceDescriptor
    ) =
        registeringDevices( device, dependentDevice ).also { (study, deployment) ->
            val deviceDeployment = deployment.getDeviceDeploymentFor( device )
            deployment.deviceDeployed( device, deviceDeployment.lastUpdatedOn )
            study.deploymentStatusReceived( deployment.getStatus() )
            study.consumeEvents()
        }

    private fun running(
        device: AnyMasterDeviceDescriptor,
        dependentDevice: AnyMasterDeviceDescriptor? = null
    ) =
        registeringDevices( device, dependentDevice ).also { (study, deployment) ->
            val deviceDeployment = deployment.getDeviceDeploymentFor( device )
            deployment.deviceDeployed( device, deviceDeployment.lastUpdatedOn )
            if ( dependentDevice != null )
            {
                val dependentDeviceDeployment = deployment.getDeviceDeploymentFor( dependentDevice )
                deployment.deviceDeployed( dependentDevice, dependentDeviceDeployment.lastUpdatedOn )
            }
            study.deploymentStatusReceived( deployment.getStatus() )
            study.consumeEvents()
        }


    private fun singleDeviceDeployment( device: AnyMasterDeviceDescriptor ) =
        StudyDeployment.fromInvitations(
            StudyProtocol( UUID.randomUUID(), "Test" ).apply { addMasterDevice( device ) }.getSnapshot(),
            listOf(
                ParticipantInvitation(
                    UUID.randomUUID(),
                    setOf( device.roleName ),
                    UsernameAccountIdentity( "Test" ),
                    StudyInvitation( "Test" )
                )
            ),
            deploymentId
        )

    private fun twoDeviceDeployment(
        device: AnyMasterDeviceDescriptor,
        dependentDevice: AnyMasterDeviceDescriptor
    ) = StudyDeployment.fromInvitations(
        StudyProtocol( UUID.randomUUID(), "Test" ).apply {
            addMasterDevice( device )
            addMasterDevice( dependentDevice )
        }.getSnapshot(),
        listOf(
            ParticipantInvitation(
                UUID.randomUUID(),
                setOf( device.roleName, dependentDevice.roleName ),
                UsernameAccountIdentity( "Test" ),
                StudyInvitation( "Test" )
            )
        ),
        deploymentId
    )


    @Test
    fun deploymentNotStarted_to_stopped()
    {
        val (study, deployment) = deploymentNotStarted( device )
        assertEquals( StudyStatus.DeploymentNotStarted( study.id ), study.getStatus() )

        val stopped: StudyDeploymentStatus = deployment.run {
            stop()
            getStatus()
        }
        study.deploymentStatusReceived( stopped )

        assertEquals(
            StudyStatus.Stopped( study.id, stopped, null ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( stopped ) )
    }

    @Test
    fun deploymentNotStarted_to_awaitingDeviceDeployment()
    {
        val (study, deployment) = deploymentNotStarted( device )

        val registered: StudyDeploymentStatus = deployment.run {
            registerDevice( device, device.createRegistration() )
            getStatus()
        }
        study.deploymentStatusReceived( registered )

        assertEquals(
            StudyStatus.AwaitingDeviceDeployment( study.id, registered ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( registered ) )
    }

    @Test
    fun deploymentNotStarted_to_awaitingOtherDeviceRegistrations()
    {
        val (study, deployment) = deploymentNotStarted( device, dependentDevice )

        val awaitingOtherRegistration: StudyDeploymentStatus = deployment.run {
            registerDevice( device, device.createRegistration() )
            getStatus()
        }
        study.deploymentStatusReceived( awaitingOtherRegistration )

        assertEquals(
            StudyStatus.AwaitingOtherDeviceRegistrations( study.id, awaitingOtherRegistration ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( awaitingOtherRegistration ) )
    }

    @Test
    fun awaitingOtherDeviceRegistrations_to_awaitingDeviceDeployment()
    {
        val (study, deployment) = awaitingOtherDeviceRegistrations( device, dependentDevice )

        val awaitingDeviceDeployment: StudyDeploymentStatus = deployment.run {
            registerDevice( dependentDevice, dependentDevice.createRegistration() )
            getStatus()
        }
        study.deploymentStatusReceived( awaitingDeviceDeployment )

        assertEquals(
            StudyStatus.AwaitingDeviceDeployment( study.id, awaitingDeviceDeployment ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( awaitingDeviceDeployment ) )
    }

    @Test
    fun awaitingDeviceDeployment_to_registeringDevices()
    {
        val (study, deployment) = awaitingDeviceDeployment( device )

        val deviceDeployment = deployment.getDeviceDeploymentFor( device )
        study.deviceDeploymentReceived( deviceDeployment )

        assertEquals(
            StudyStatus.RegisteringDevices( study.id, deployment.getStatus(), deviceDeployment ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeviceDeploymentReceived( deviceDeployment ) )
    }

    @Test
    fun registeringDevices_to_running()
    {
        val (study, deployment) = registeringDevices( device )

        val deviceDeployment = deployment.getDeviceDeploymentFor( device )
        deployment.deviceDeployed( device, deviceDeployment.lastUpdatedOn )
        val running = deployment.getStatus()
        study.deploymentStatusReceived( running )

        assertEquals(
            StudyStatus.Running( study.id, running, deviceDeployment ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( running ) )
    }

    @Test
    fun registeringDevices_to_awaitingOtherDeviceDeployments()
    {
        val (study, deployment) = registeringDevices( device, dependentDevice )

        val deviceDeployment = deployment.getDeviceDeploymentFor( device )
        deployment.deviceDeployed( device, deviceDeployment.lastUpdatedOn )
        val awaitingOther = deployment.getStatus()
        study.deploymentStatusReceived( awaitingOther )

        assertEquals(
            StudyStatus.AwaitingOtherDeviceDeployments( study.id, awaitingOther, deviceDeployment ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( awaitingOther ) )
    }

    @Test
    fun awaitingOtherDeviceDeployments_to_running()
    {
        val (study, deployment) = awaitingOtherDeviceDeployments( device, dependentDevice )

        val deviceDeployment = deployment.getDeviceDeploymentFor( dependentDevice )
        deployment.deviceDeployed( dependentDevice, deviceDeployment.lastUpdatedOn )
        val running = deployment.getStatus()
        study.deploymentStatusReceived( running )

        assertEquals(
            StudyStatus.Running( study.id, running, deployment.getDeviceDeploymentFor( device ) ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( running ) )
    }

    @Test
    fun running_to_stopped()
    {
        val (study, deployment) = running( device )

        deployment.stop()
        val stopped = deployment.getStatus()
        study.deploymentStatusReceived( stopped )

        assertEquals(
            StudyStatus.Stopped( study.id, stopped, deployment.getDeviceDeploymentFor( device ) ),
            study.getStatus()
        )
        assertSingleEvent( study, Study.Event.DeploymentStatusReceived( stopped ) )
    }

    @Test
    fun creating_study_fromSnapshot_obtained_by_getSnapshot_is_the_same() = runSuspendTest {
        // Create a study snapshot for the 'smartphone' with an unregistered connected device.
        val deploymentId = UUID.randomUUID()
        val study = Study( deploymentId, smartphone.roleName )
        val connectedDevices = setOf( connectedDevice )
        val masterDeviceDeployment = MasterDeviceDeployment(
            smartphone,
            smartphone.createRegistration(),
            connectedDevices
        )
        study.deploymentStatusReceived(
            StudyDeploymentStatus.DeployingDevices(
                Clock.System.now(),
                deploymentId,
                listOf(
                    DeviceDeploymentStatus.Registered(
                        smartphone,
                        true,
                        emptySet(),
                        connectedDevices.map { it.roleName }.toSet()
                    )
                ),
                emptyList(),
                null
            )
        )
        study.deviceDeploymentReceived( masterDeviceDeployment )
        val snapshot = study.getSnapshot()
        val fromSnapshot = Study.fromSnapshot( snapshot )

        assertEquals( study.studyDeploymentId, fromSnapshot.studyDeploymentId )
        assertEquals( study.createdOn, fromSnapshot.createdOn )
        assertEquals( study.deviceRoleName, fromSnapshot.deviceRoleName )
        assertEquals( study.getStatus(), fromSnapshot.getStatus() )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }

    private fun assertSingleEvent( study: Study, event: Study.Event ) =
        assertEquals( event, study.consumeEvents().singleOrNull() )
}
