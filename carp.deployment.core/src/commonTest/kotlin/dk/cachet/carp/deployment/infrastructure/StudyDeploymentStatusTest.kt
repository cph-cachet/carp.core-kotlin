package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.*
import kotlin.test.*


/**
 * Tests for [StudyDeploymentStatus] relying on core infrastructure.
 */
class StudyDeploymentStatusTest
{
    private val testId = UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" )

    private fun createSingleMasterWithConnectedDeviceDeployment(): StudyDeployment
    {
        val protocol = createSingleMasterWithConnectedDeviceProtocol()
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        return StudyDeployment( snapshot, testId )
    }


    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createDeploymentSerializer( STUBS_SERIAL_MODULE )
    }

    @Test
    fun can_serialize_and_deserialize_deployment_status_using_JSON()
    {
        val deployment = createSingleMasterWithConnectedDeviceDeployment()
        val status: StudyDeploymentStatus = deployment.getStatus()

        val serialized: String = status.toJson()
        val parsed: StudyDeploymentStatus = StudyDeploymentStatus.fromJson( serialized )

        assertEquals( status, parsed )
    }

    @Test
    fun serializing_deployment_when_unknown_devices_are_involved()
    {
        val protocol = createEmptyProtocol()
        protocol.addMasterDevice( UnknownMasterDeviceDescriptor( "Unknown" ) )
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        var serialized: String = snapshot.toJson()

        // Replace the strings which identify the types to load by the PolymorphicSerializer.
        // This will cause the types not to be found while deserializing, hence mimicking 'custom' types.
        serialized = serialized.replace( "dk.cachet.carp.deployment.domain.UnknownMasterDeviceDescriptor", "com.unknown.CustomMasterDevice" )

        // Create deployment based on protocol with custom types and serialize its status.
        val snapshotWithCustom = StudyProtocolSnapshot.fromJson( serialized )
        val deployment = StudyDeployment( snapshotWithCustom, testId )
        val status = deployment.getStatus().toJson()

        // This verifies whether the 'CustomMasterDeviceDescriptor' wrapper is removed in JSON output.
        assertTrue { status.contains( """"device":["com.unknown.CustomMasterDevice",{""" ) }
    }
}