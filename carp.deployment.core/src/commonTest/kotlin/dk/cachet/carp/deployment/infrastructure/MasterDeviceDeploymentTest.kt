package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.STUBS_SERIAL_MODULE
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTrigger
import kotlin.test.*


/**
 * Tests for [MasterDeviceDeployment] relying on core infrastructure.
 */
class MasterDeviceDeploymentTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createDeploymentSerializer( STUBS_SERIAL_MODULE )
    }

    @Test
    fun can_serialize_and_deserialize_devicedeployment_using_JSON()
    {
        val masterRegistration = DefaultDeviceRegistration( "0" )
        val connected = StubDeviceDescriptor( "Connected" )
        val connectedRegistration = DefaultDeviceRegistration( "1" )
        val task = StubTaskDescriptor( "Task" )
        val trigger = StubTrigger( "Connected" )

        val deployment = MasterDeviceDeployment(
            masterRegistration,
            setOf( connected ),
            mapOf( connected.roleName to connectedRegistration ),
            setOf( task ),
            mapOf( 0 to trigger ),
            setOf( MasterDeviceDeployment.TriggeredTask( 0, task.name, connected.roleName ) )
        )

        val json = deployment.toJson()
        val parsed = MasterDeviceDeployment.fromJson( json )
        assertEquals( deployment, parsed )
    }
}
