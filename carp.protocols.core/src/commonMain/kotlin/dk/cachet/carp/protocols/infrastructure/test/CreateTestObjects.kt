package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.TaskDescriptor
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.domain.triggers.Trigger
import dk.cachet.carp.protocols.infrastructure.JSON
import dk.cachet.carp.protocols.infrastructure.PROTOCOLS_SERIAL_MODULE
import dk.cachet.carp.protocols.infrastructure.createProtocolsSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlin.reflect.KClass


/**
 * Stubs for testing extending from types in [dk.cachet.carp.protocols] module which need to be registered when using [Json] serializer.
 */
val STUBS_SERIAL_MODULE = SerializersModule {
    polymorphic( DeviceDescriptor::class )
    {
        StubDeviceDescriptor::class with StubDeviceDescriptor.serializer()
    }
    polymorphic( MasterDeviceDescriptor::class, DeviceDescriptor::class )
    {
        StubMasterDeviceDescriptor::class with StubMasterDeviceDescriptor.serializer()
    }
    polymorphic( SamplingConfiguration::class )
    {
        StubSamplingConfiguration::class with StubSamplingConfiguration.serializer()
    }
    polymorphic( TaskDescriptor::class )
    {
        StubTaskDescriptor::class with StubTaskDescriptor.serializer()
    }
    polymorphic( Measure::class )
    {
        StubMeasure::class with StubMeasure.serializer()
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

    val alwaysSameOwner = ProtocolOwner( UUID( "27879e75-ccc1-4866-9ab3-4ece1b735052" ) )
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

/**
 * Replace the type name of [deviceDescriptor] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    deviceDescriptor: AnyDeviceDescriptor,
    unknownTypeName: String = "com.unknown.UnknownDeviceDescriptor"
): String =
    this.makeUnknown( deviceDescriptor, DeviceDescriptor::class, "roleName", deviceDescriptor.roleName, unknownTypeName )

/**
 * Replace the type name of [masterDeviceDescriptor] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    masterDeviceDescriptor: AnyMasterDeviceDescriptor,
    unknownTypeName: String = "com.unknown.UnknownMasterDeviceDescriptor"
): String =
    this.makeUnknown( masterDeviceDescriptor, MasterDeviceDescriptor::class, "roleName", masterDeviceDescriptor.roleName, unknownTypeName )

/**
 * Replace the type name of [registration] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    registration: DeviceRegistration,
    unknownTypeName: String = "com.unknown.UnknownDeviceRegistration"
): String =
    this.makeUnknown( registration, DeviceRegistration::class, "deviceId", registration.deviceId, unknownTypeName )

/**
 * Replace the type name of [taskDescriptor] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    taskDescriptor: TaskDescriptor,
    unknownTypeName: String = "com.unknown.UnknownTaskDescriptor"
): String =
    this.makeUnknown( taskDescriptor, TaskDescriptor::class, "name", taskDescriptor.name, unknownTypeName )

/**
 * Replace the type name of the [measure] with the specified [key] set to [value] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    measure: Measure,
    key: String,
    value: String,
    unknownTypeName: String = "com.unknown.UnknownMeasure"
): String =
    this.makeUnknown( measure, Measure::class, key, value, unknownTypeName )

/**
 * Replace the type name of the [samplingConfiguration] with the specified [key] set to [value] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    samplingConfiguration: SamplingConfiguration,
    key: String,
    value: String,
    unknownTypeName: String = "com.unknown.UnknownSamplingConfiguration"
): String =
    this.makeUnknown( samplingConfiguration, SamplingConfiguration::class, key, value, unknownTypeName )

/**
 * Replace the type name of the [trigger] with the specified [key] set to [value] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    trigger: Trigger,
    key: String,
    value: String,
    unknownTypeName: String = "com.unknown.UnknownTrigger"
): String =
    this.makeUnknown( trigger, Trigger::class, key, value, unknownTypeName )

private fun <T : Any> String.makeUnknown( instance: T, klass: KClass<T>, key: String, value: String, unknownTypeName: String ): String
{
    // Get qualified type name.
    val serialModule = PROTOCOLS_SERIAL_MODULE.plus( STUBS_SERIAL_MODULE )
    val serializer = serialModule.getPolymorphic( klass, instance )
    val qualifiedName = serializer!!.descriptor.serialName

    // Construct regex to identify the object with the qualified name, and with the matching key/value pair set.
    // TODO: This regex uses negative lookahead to filter out JSON which contains multiple types with the same name.
    //       This is complex, and furthermore not 100% foolproof in rare cases (e.g., if the string is used not as a type name).
    //       Probably this should be rewritten with a JSON parser.
    val escapedQualifiedName = qualifiedName.replace( ".", "\\." )
    val objectRegex = Regex("(\\[\")($escapedQualifiedName)(\",\\{(?!.*?$escapedQualifiedName.*?\"$key\":\"${value}\").*?\"$key\":\"${value}\".*?\\})" )

    // Replace type name with an unknown type name to mimic it is not available at runtime.
    val match = objectRegex.find( this )
    require( match != null && match.groups.count() == 4 ) { "Could not find the specified object in the serialized string." }
    return this.replace( objectRegex, "$1$unknownTypeName$3" )
}
