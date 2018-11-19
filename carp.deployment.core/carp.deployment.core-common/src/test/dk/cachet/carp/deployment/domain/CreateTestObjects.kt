package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


val testId = UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" )


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers).
 */
fun createEmptyProtocol(): StudyProtocol
{
    val alwaysSameOwner = ProtocolOwner( UUID( "f3f4d91b-56b5-4117-bb98-7e2923cb2223" ) )
    return StudyProtocol( alwaysSameOwner, "Test protocol" )
}

/**
 * Creates a study protocol with a single master device which has a single connected device.
 */
fun createSingleMasterWithConnectedDeviceProtocol(
    masterDeviceName: String = "Master",
    connectedDeviceName: String = "Connected" ): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val master = StubMasterDeviceDescriptor( masterDeviceName )
    protocol.addMasterDevice( master )
    protocol.addConnectedDevice( StubDeviceDescriptor( connectedDeviceName ), master )
    return protocol
}

fun deploymentFor( protocol: StudyProtocol ): StudyDeployment
{
    val snapshot = protocol.getSnapshot()
    return StudyDeployment( snapshot, testId )
}

@Serializable
internal data class UnknownDeviceDescriptor( override val roleName: String ) : DeviceDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownDeviceDescriptor::class,
                UnknownDeviceDescriptor.serializer(),
                "dk.cachet.carp.deployment.domain.UnknownDeviceDescriptor" )
        }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownMasterDeviceDescriptor( override val roleName: String ) : MasterDeviceDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownMasterDeviceDescriptor::class,
                UnknownMasterDeviceDescriptor.serializer(),
                "dk.cachet.carp.deployment.domain.UnknownMasterDeviceDescriptor" )
        }
    }

    override fun createRegistration(): DeviceRegistration = defaultDeviceRegistration()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal class UnknownDeviceRegistration( override var deviceId: String ) : DeviceRegistration()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownDeviceRegistration::class,
                UnknownDeviceRegistration.serializer(),
                "dk.cachet.carp.deployment.domain.UnknownDeviceRegistration" )
        }
    }
}