package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol


/**
 * Creates a study protocol with a single master device which has a single connected device.
 */
fun createSingleMasterWithConnectedDeviceProtocol(
    masterDeviceName: String = "Master",
    connectedDeviceName: String = "Connected"
): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val master = StubMasterDeviceDescriptor( masterDeviceName )
    protocol.addMasterDevice( master )
    protocol.addConnectedDevice( StubDeviceDescriptor( connectedDeviceName ), master )
    return protocol
}

fun studyDeploymentFor( protocol: StudyProtocol ): StudyDeployment
{
    val snapshot = protocol.getSnapshot()
    return StudyDeployment( snapshot )
}

/**
 * Creates a study deployment with a registered device and participation added.
 */
fun createComplexDeployment(): StudyDeployment
{
    val protocol = createSingleMasterWithConnectedDeviceProtocol( "Master", "Connected" )
    val deployment = studyDeploymentFor( protocol )

    // Add device registrations.
    val master = deployment.registrableDevices.first { it.device.roleName == "Master" }.device as AnyMasterDeviceDescriptor
    val connected = deployment.registrableDevices.first { it.device.roleName == "Connected" }.device
    deployment.registerDevice( master, master.createRegistration() )
    deployment.registerDevice( connected, connected.createRegistration() )

    // Add a participation.
    val account = Account.withUsernameIdentity( "test" )
    val participation = Participation( deployment.id )
    deployment.addParticipation( account, participation )

    // Deploy a device.
    val deviceDeployment = deployment.getDeviceDeploymentFor( master )
    deployment.deviceDeployed( master, deviceDeployment.getChecksum() )

    deployment.stop()

    // Remove events since tests building on top of this are not interested in how this object was constructed.
    deployment.consumeEvents()

    return deployment
}
