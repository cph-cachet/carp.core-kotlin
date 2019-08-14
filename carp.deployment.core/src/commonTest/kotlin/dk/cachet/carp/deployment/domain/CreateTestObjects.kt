package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.NotSerializable
import dk.cachet.carp.deployment.domain.triggers.StubTrigger
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.triggers.Trigger
import dk.cachet.carp.protocols.infrastructure.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass


val testId = UUID( "27c56423-b7cd-48dd-8b7f-f819621a34f0" )

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
    connectedDeviceName: String = "Connected" ): StudyProtocol
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
    return StudyDeployment( snapshot, testId )
}

@Serializable
internal data class UnknownDeviceDescriptor( override val roleName: String )
    : DeviceDescriptor<DeviceRegistration, UnknownDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): UnknownDeviceRegistrationBuilder = UnknownDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownMasterDeviceDescriptor( override val roleName: String )
    : MasterDeviceDescriptor<DeviceRegistration, UnknownDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): UnknownDeviceRegistrationBuilder = UnknownDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownDeviceRegistration( override val deviceId: String ) : DeviceRegistration()

@Serializable( with = NotSerializable::class )
@DeviceRegistrationBuilderDsl
class UnknownDeviceRegistrationBuilder( private var deviceId: String = UUID.randomUUID().toString() )
    : DeviceRegistrationBuilder<DeviceRegistration>()
{
    override fun build(): DeviceRegistration = DefaultDeviceRegistration( deviceId )
}