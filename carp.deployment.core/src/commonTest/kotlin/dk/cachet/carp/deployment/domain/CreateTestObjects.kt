package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.serialization.NotSerializable
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.deployment.domain.triggers.StubTrigger
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.DeviceRegistrationBuilder
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.triggers.Trigger
import dk.cachet.carp.protocols.infrastructure.createProtocolsSerializer
import dk.cachet.carp.protocols.infrastructure.JSON
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass


/**
 * Stubs for testing extending from types in [dk.cachet.carp.protocols] module which need to be registered when using [Json] serializer.
 */
internal val STUBS_SERIAL_MODULE = SerializersModule {
    polymorphic( DeviceDescriptor::class )
    {
        StubDeviceDescriptor::class with StubDeviceDescriptor.serializer()
        UnknownDeviceDescriptor::class with UnknownDeviceDescriptor.serializer()
    }
    polymorphic( MasterDeviceDescriptor::class, DeviceDescriptor::class )
    {
        StubMasterDeviceDescriptor::class with StubMasterDeviceDescriptor.serializer()
        UnknownMasterDeviceDescriptor::class with UnknownMasterDeviceDescriptor.serializer()
    }
    polymorphic ( DeviceRegistration::class )
    {
        UnknownDeviceRegistration::class with UnknownDeviceRegistration.serializer()
    }
    polymorphic( TaskDescriptor::class )
    {
        StubTaskDescriptor::class with StubTaskDescriptor.serializer()
    }
    polymorphic( Trigger::class )
    {
        StubTrigger::class with StubTrigger.serializer()
    }
}


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers),
 * and initializes the infrastructure serializer to be aware about polymorph stub testing classes.
 */
fun createEmptyProtocol(): StudyProtocol
{
    JSON = createProtocolsSerializer( STUBS_SERIAL_MODULE )

    val alwaysSameOwner = ProtocolOwner( UUID( "f3f4d91b-56b5-4117-bb98-7e2923cb2223" ) )
    return StudyProtocol( alwaysSameOwner, "Test protocol" )
}

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
    val protocol = createSingleMasterWithConnectedDeviceProtocol()
    val deployment = studyDeploymentFor( protocol )

    // Add device registration.
    val master = deployment.registrableDevices.first().device as AnyMasterDeviceDescriptor
    deployment.registerDevice( master, DefaultDeviceRegistration( "test" ) )

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

@Serializable
internal data class UnknownDeviceDescriptor( override val roleName: String ) :
    DeviceDescriptor<DeviceRegistration, UnknownDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): UnknownDeviceRegistrationBuilder = UnknownDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownMasterDeviceDescriptor( override val roleName: String ) :
    MasterDeviceDescriptor<DeviceRegistration, UnknownDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): UnknownDeviceRegistrationBuilder = UnknownDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownDeviceRegistration( override val deviceId: String ) : DeviceRegistration()

@Serializable( with = NotSerializable::class )
class UnknownDeviceRegistrationBuilder( private var deviceId: String = UUID.randomUUID().toString() ) :
    DeviceRegistrationBuilder<DeviceRegistration>
{
    override fun build(): DeviceRegistration = DefaultDeviceRegistration( deviceId )
}
