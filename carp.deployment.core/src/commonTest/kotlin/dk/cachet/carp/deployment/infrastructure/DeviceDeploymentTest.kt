package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.deployment.domain.triggers.StubTrigger
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import kotlin.test.*


/**
 * Tests for [DeviceDeployment] relying on core infrastructure.
 */
class DeviceDeploymentTest
{
    @Test
    fun can_serialize_and_deserialize_devicedeployment_using_JSON()
    {
        val masterRegistration = DefaultDeviceRegistration( "0" )
        val connected = StubDeviceDescriptor( "Connected" )
        val connectedRegistration = DefaultDeviceRegistration( "1" )
        val task = StubTaskDescriptor( "Task" )
        val trigger = StubTrigger( "Connected" )

        val deployment = DeviceDeployment(
            masterRegistration,
            setOf( connected ),
            mapOf( connected.roleName to connectedRegistration ),
            setOf( task ),
            mapOf( 0 to trigger ),
            setOf( DeviceDeployment.TriggeredTask(0, task.name, connected.roleName ) )
        )

        val json = deployment.toJson()
        val parsed = DeviceDeployment.fromJson( json )
        assertEquals( deployment, parsed )
    }
}