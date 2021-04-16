package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.fromJson
import dk.cachet.carp.protocols.infrastructure.toJson
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterWithConnectedDeviceProtocol
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.*


/**
 * Tests for [StudyDeploymentStatus] relying on core infrastructure.
 */
class StudyDeploymentStatusTest
{
    private val testId = UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" )


    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createTestJSON()
    }

    @Test
    fun can_serialize_and_deserialize_deployment_status_using_JSON()
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val master = protocol.masterDevices.single()
        val deployment = StudyDeployment( protocol.getSnapshot(), testId )
        deployment.registrableDevices.forEach {
            deployment.registerDevice( it.device, it.device.createRegistration() )
        }
        val masterDeviceDeployment = deployment.getDeviceDeploymentFor( master )
        deployment.deviceDeployed( master, masterDeviceDeployment.lastUpdateDate )
        val status: StudyDeploymentStatus = deployment.getStatus()

        val serialized: String = status.toJson()
        val parsed: StudyDeploymentStatus = StudyDeploymentStatus.fromJson( serialized )

        assertEquals( status, parsed )
    }

    @ExperimentalSerializationApi
    @Test
    fun serializing_deployment_when_unknown_devices_are_involved()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor( "Unknown" )
        protocol.addMasterDevice( master )
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        var serialized: String = snapshot.toJson()

        // Mimic an unknown device type.
        serialized = serialized.makeUnknown( master, "com.unknown.UnknownMasterDevice" )

        // Create deployment based on protocol with custom types and serialize its status.
        val snapshotWithCustom = StudyProtocolSnapshot.fromJson( serialized )
        val deployment = StudyDeployment( snapshotWithCustom, testId )
        val status = deployment.getStatus().toJson()

        // This verifies whether the 'CustomMasterDeviceDescriptor' wrapper is removed in JSON output.
        assertTrue { status.contains( "\"device\":{\"$CLASS_DISCRIMINATOR\":\"com.unknown.UnknownMasterDevice\"" ) }
    }
}
