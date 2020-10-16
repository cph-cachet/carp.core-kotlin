package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.DataListener
import dk.cachet.carp.client.domain.data.StubConnectedDeviceDataCollectorFactory
import dk.cachet.carp.client.infrastructure.fromJson
import dk.cachet.carp.client.infrastructure.toJson
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.protocols.domain.devices.AltBeaconDeviceRegistration
import dk.cachet.carp.protocols.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMeasure
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [StudyRuntime].
 */
class StudyRuntimeTest
{
    @Test
    fun initialize_matches_requested_runtime() = runBlockingTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        assertEquals( deploymentStatus.studyDeploymentId, runtime.studyDeploymentId )
        assertEquals( smartphone.roleName, runtime.device.roleName )
    }

    @Test
    fun initialize_deploys_when_possible() = runBlockingTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val updatedStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )

        assertTrue( runtime.isDeployed )
        assertEquals( 1, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertTrue( updatedStatus.getDeviceStatus( smartphone ) is DeviceDeploymentStatus.Deployed )
    }

    @Test
    fun initialize_does_not_deploy_when_depending_on_other_devices() = runBlockingTest {
        // Create a deployment service which contains a study where 'smartphone' depends on another master device.
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val updatedStatus = deploymentService.getStudyDeploymentStatus( deploymentStatus.studyDeploymentId )

        assertFalse( runtime.isDeployed )
        assertEquals( 0, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertTrue( updatedStatus.getDeviceStatus( smartphone ) is DeviceDeploymentStatus.NotDeployed )
    }

    @Test
    fun initialize_fails_for_unknown_studyDeploymentId() = runBlockingTest {
        // Create a deployment service which contains a 'smartphone study'.
        val (deploymentService, _) = createStudyDeployment( createSmartphoneStudy() )

        val unknownId = UUID.randomUUID()
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        assertFailsWith<IllegalArgumentException> {
            StudyRuntime.initialize(
                deploymentService, dataListener,
                unknownId, smartphone.roleName, deviceRegistration )
        }
    }

    @Test
    fun initialize_fails_for_unknown_deviceRoleName() = runBlockingTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        assertFailsWith<IllegalArgumentException> {
            StudyRuntime.initialize(
                deploymentService, dataListener,
                deploymentStatus.studyDeploymentId, "Unknown role", deviceRegistration )
        }
    }

    @Test
    fun initialize_fails_for_incorrect_deviceRegistration() = runBlockingTest {
        val (deploymentService, deploymentStatus) = createStudyDeployment( createSmartphoneStudy() )

        val incorrectRegistration = AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0 )
        val dataListener = createDataListener()
        assertFailsWith<IllegalArgumentException> {
            StudyRuntime.initialize(
                deploymentService, dataListener,
                deploymentStatus.studyDeploymentId, smartphone.roleName, incorrectRegistration )
        }
    }

    @Test
    fun tryDeployment_only_succeeds_after_dependent_devices_are_registered() = runBlockingTest {
        // Create a study runtime for a study where 'smartphone' depends on another master device ('deviceSmartphoneDependsOn').
        val (deploymentService, deploymentStatus) = createStudyDeployment( createDependentSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )

        // Dependent devices are not yet registered.
        var wasDeployed = runtime.tryDeployment( deploymentService, dataListener )
        assertEquals( 0, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertFalse( wasDeployed )

        // Once dependent devices are registered, deployment succeeds.
        deploymentService.registerDevice( deploymentStatus.studyDeploymentId, deviceSmartphoneDependsOn.roleName, deviceSmartphoneDependsOn.createRegistration() )
        wasDeployed = runtime.tryDeployment( deploymentService, dataListener )
        assertEquals( 1, runtime.consumeEvents().filterIsInstance<StudyRuntime.Event.Deployed>().count() )
        assertTrue( wasDeployed )
    }

    @Test
    fun tryDeployment_succeeds_when_data_types_of_protocol_measures_are_supported() = runBlockingTest {
        // Create protocol that measures on smartphone and one connected device.
        val protocol = createSmartphoneStudy()
        val connectedDevice = StubDeviceDescriptor( "Connected" )
        protocol.addConnectedDevice( connectedDevice, smartphone )
        val masterTask = StubTaskDescriptor( "Master measure", listOf( StubMeasure( STUB_DATA_TYPE ) ) )
        protocol.addTriggeredTask( smartphone.atStartOfStudy(), masterTask, smartphone )
        val connectedDataType = DataType( "custom", "type" )
        val connectedTask = StubTaskDescriptor( "Connected measure", listOf( StubMeasure( connectedDataType ) ) )
        protocol.addTriggeredTask( smartphone.atStartOfStudy(), connectedTask, connectedDevice )

        // Create a data listener which supports the requested devices and types in the protocol
        val dataListener = DataListener( StubConnectedDeviceDataCollectorFactory(
            localSupportedDataTypes = setOf( STUB_DATA_TYPE ),
            mapOf( StubDeviceDescriptor::class to setOf( connectedDataType ) )
        ) )

        // Initializing study runtime for the smartphone deployment should succeed since devices and data types are supported.
        val (deploymentService, deploymentStatus) = createStudyDeployment( protocol )
        val deviceRegistration = smartphone.createRegistration()
        StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
    }

    @Test
    fun tryDeployment_fails_when_requested_data_cannot_be_collected() = runBlockingTest {
        // Create a protocol that has one measure.
        val protocol = createSmartphoneStudy()
        val task = StubTaskDescriptor( "One measure", listOf( StubMeasure() ) )
        protocol.addTriggeredTask( smartphone.atStartOfStudy(), task, smartphone )

        // Initializing study runtime for the smartphone deployment should fail since StubMeasure can't be collected.
        val (deploymentService, deploymentStatus) = createStudyDeployment( protocol )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener( supportedDataTypes = emptyArray() )
        assertFailsWith<UnsupportedOperationException>
        {
            StudyRuntime.initialize(
                deploymentService, dataListener,
                deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        }
    }

    @Test
    fun creating_runtime_fromSnapshot_obtained_by_getSnapshot_is_the_same() = runBlockingTest {
        // Create a study runtime snapshot for the 'smartphone' in 'smartphone study'.
        val ( deploymentService, deploymentStatus ) = createStudyDeployment( createSmartphoneStudy() )
        val deviceRegistration = smartphone.createRegistration()
        val dataListener = createDataListener()
        val runtime = StudyRuntime.initialize(
            deploymentService, dataListener,
            deploymentStatus.studyDeploymentId, smartphone.roleName, deviceRegistration )
        val snapshot = StudyRuntimeSnapshot.fromStudyRuntime( runtime )

        val serialized = snapshot.toJson()
        val parsed = StudyRuntimeSnapshot.fromJson( serialized )

        assertEquals( runtime.studyDeploymentId, parsed.studyDeploymentId )
        assertEquals( runtime.creationDate, parsed.creationDate )
        assertEquals( runtime.device, parsed.device )
        assertEquals( runtime.isDeployed, parsed.isDeployed )
        assertEquals( runtime.deploymentInformation, parsed.deploymentInformation )
    }
}
