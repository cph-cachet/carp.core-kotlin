package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.*
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.common.serialization.*
import dk.cachet.carp.protocols.domain.triggers.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.tasks.measures.*
import kotlinx.serialization.Serializable


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers).
 */
fun createEmptyProtocol(): StudyProtocol
{
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
internal data class UnknownMasterDeviceDescriptor( override val roleName: String ) : MasterDeviceDescriptor<DefaultDeviceRegistrationBuilder>()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownMasterDeviceDescriptor::class,
                UnknownMasterDeviceDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownMasterDeviceDescriptor" )
        }
    }

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownDeviceDescriptor( override val roleName: String ) : DeviceDescriptor<DefaultDeviceRegistrationBuilder>()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownDeviceDescriptor::class,
                UnknownDeviceDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownDeviceDescriptor" )
        }
    }

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder()
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.TRUE
}

@Serializable
internal data class UnknownDeviceRegistration( override val deviceId: String ) : DeviceRegistration()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownDeviceRegistration::class,
                UnknownDeviceRegistration.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownDeviceRegistration" )
        }
    }
}

@Serializable
internal data class UnknownTaskDescriptor(
    override val name: String,
    @Serializable( PolymorphicArrayListSerializer::class )
    override val measures: List<Measure> ) : TaskDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownTaskDescriptor::class,
                UnknownTaskDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownTaskDescriptor" )
        }
    }
}

@Serializable
internal data class UnknownMeasure( override val type: DataType ) : Measure()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownMeasure::class,
                UnknownMeasure.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownMeasure" )
        }
    }
}

@Serializable
internal data class UnknownTrigger( override val sourceDeviceRoleName: String ) : Trigger()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                UnknownTrigger::class,
                UnknownTrigger.serializer(),
                "dk.cachet.carp.protocols.domain.UnknownTrigger" )
        }
    }
}

