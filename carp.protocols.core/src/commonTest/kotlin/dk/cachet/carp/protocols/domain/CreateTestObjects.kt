package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.common.serialization.NotSerializable
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.triggers.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import dk.cachet.carp.protocols.infrastructure.*
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
        UnknownTaskDescriptor::class with UnknownTaskDescriptor.serializer()
    }
    polymorphic( Measure::class )
    {
        StubMeasure::class with StubMeasure.serializer()
        UnknownMeasure::class with UnknownMeasure.serializer()
    }
    polymorphic( Trigger::class )
    {
        StubTrigger::class with StubTrigger.serializer()
        UnknownTrigger::class with UnknownTrigger.serializer()
    }
}


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers),
 * and initializes the infrastructure serializer to be aware about polymorph stub testing classes.
 */
fun createEmptyProtocol(): StudyProtocol
{
    JSON = createProtocolsSerializer( STUBS_SERIAL_MODULE )

    val alwaysSameOwner = ProtocolOwner( UUID( "27879e75-ccc1-4866-9ab3-4ece1b735052" ) )
    return StudyProtocol( alwaysSameOwner, "Test protocol" )
}

/**
 * Creates a study protocol with a couple of devices and tasks added.
 */
fun createComplexProtocol(): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val masterDevice = StubMasterDeviceDescriptor()
    val connectedDevice = StubDeviceDescriptor()
    val chainedMasterDevice = StubMasterDeviceDescriptor( "Chained master" )
    val chainedConnectedDevice = StubDeviceDescriptor( "Chained connected" )
    val trigger = StubTrigger( connectedDevice )
    val measures = listOf( StubMeasure() )
    val task = StubTaskDescriptor( "Task", measures )
    with ( protocol )
    {
        addMasterDevice( masterDevice )
        addConnectedDevice( connectedDevice, masterDevice )
        addConnectedDevice( chainedMasterDevice, masterDevice )
        addConnectedDevice( chainedConnectedDevice, chainedMasterDevice )
        addTriggeredTask( trigger, task, masterDevice )
    }

    return protocol
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
internal data class UnknownDeviceDescriptor( override val roleName: String )
    : DeviceDescriptor<DeviceRegistration, UnknownDeviceRegistrationBuilder>()
{
    override fun createDeviceRegistrationBuilder(): UnknownDeviceRegistrationBuilder = UnknownDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable( with = NotSerializable::class )
@DeviceRegistrationBuilderDsl
class UnknownDeviceRegistrationBuilder( private var deviceId: String = UUID.randomUUID().toString() )
    : DeviceRegistrationBuilder<DeviceRegistration>()
{
    override fun build(): DeviceRegistration = DefaultDeviceRegistration( deviceId )
}

@Serializable
internal data class UnknownDeviceRegistration( override val deviceId: String ) : DeviceRegistration()

@Serializable
internal data class UnknownTaskDescriptor(
    override val name: String,
    override val measures: List<Measure> ) : TaskDescriptor()

@Serializable
internal data class UnknownMeasure( override val type: DataType ) : Measure()

@Serializable
internal data class UnknownTrigger( override val sourceDeviceRoleName: String ) : Trigger()