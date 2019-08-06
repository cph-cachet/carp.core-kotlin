package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.*
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

        val deployment = DeviceDeployment(
            masterRegistration,
            setOf( connected ),
            mapOf( Pair( "Connected", connectedRegistration ) ),
            setOf( task )
        )

        val json = deployment.toJson()
        val parsed = DeviceDeployment.fromJson( json )
        assertEquals( deployment, parsed )
    }
}