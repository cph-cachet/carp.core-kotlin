package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTrigger
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import kotlin.test.*


/**
 * Tests for [MasterDeviceDeployment] relying on core infrastructure.
 */
class MasterDeviceDeploymentTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createTestJSON()
    }

    @Test
    fun can_serialize_and_deserialize_devicedeployment_using_JSON()
    {
        val device = StubMasterDeviceDescriptor()
        val connected = StubDeviceDescriptor( "Connected" )
        val task = StubTaskDescriptor( "Task" )
        val trigger = StubTrigger( connected.roleName )

        val deployment = MasterDeviceDeployment(
            device,
            device.createRegistration(),
            setOf( connected ),
            mapOf( connected.roleName to connected.createRegistration() ),
            setOf( task ),
            mapOf( 0 to trigger ),
            setOf( MasterDeviceDeployment.TriggeredTask( 0, task.name, connected.roleName ) )
        )

        val json = deployment.toJson()
        val parsed = MasterDeviceDeployment.fromJson( json )
        assertEquals( deployment, parsed )
    }
}
