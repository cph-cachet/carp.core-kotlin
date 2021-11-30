package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.MasterDeviceDescriptor
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.Trigger
import dk.cachet.carp.common.infrastructure.serialization.COMMON_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass


/**
 * Stubs for testing extending from types in [dk.cachet.carp.common] module which need to be registered when using [Json] serializer.
 */
val STUBS_SERIAL_MODULE = SerializersModule {
    polymorphic( Data::class )
    {
        subclass( StubData::class )
        subclass( StubDataPoint::class )
        subclass( StubDataTimeSpan::class )
    }

    fun PolymorphicModuleBuilder<AnyMasterDeviceDescriptor>.registerMasterDeviceDescriptorSubclasses()
    {
        subclass( StubMasterDeviceDescriptor::class )
    }

    polymorphic( DeviceDescriptor::class )
    {
        subclass( StubDeviceDescriptor::class )
        registerMasterDeviceDescriptorSubclasses()
    }
    polymorphic( MasterDeviceDescriptor::class )
    {
        registerMasterDeviceDescriptorSubclasses()
    }
    polymorphic( SamplingConfiguration::class )
    {
        subclass( StubSamplingConfiguration::class )
    }
    polymorphic( TaskDescriptor::class )
    {
        subclass( StubTaskDescriptor::class )
    }
    polymorphic( Trigger::class )
    {
        subclass( StubTrigger::class )
    }
}


/**
 * Create a [Json] serializer with all stub types registered for polymorphic serialization.
 */
fun createTestJSON(): Json = createDefaultJSON( STUBS_SERIAL_MODULE )

/**
 * Replace the type name of [data] in this JSON string with [unknownTypeName].
 */
fun String.makeUnknown(
    data: StubData,
    unknownTypeName: String = "com.unknown.UnknownData"
): String =
    this.makeUnknown( data, Data::class, "data", data.data, unknownTypeName )

/**
 * Replace the type name of [deviceDescriptor] in this JSON string with [unknownTypeName].
 */
@ExperimentalSerializationApi
fun String.makeUnknown(
    deviceDescriptor: AnyDeviceDescriptor,
    unknownTypeName: String = "com.unknown.UnknownDeviceDescriptor"
): String =
    this.makeUnknown( deviceDescriptor, DeviceDescriptor::class, "roleName", deviceDescriptor.roleName, unknownTypeName )

/**
 * Replace the type name of [masterDeviceDescriptor] in this JSON string with [unknownTypeName].
 */
@ExperimentalSerializationApi
fun String.makeUnknown(
    masterDeviceDescriptor: AnyMasterDeviceDescriptor,
    unknownTypeName: String = "com.unknown.UnknownMasterDeviceDescriptor"
): String =
    this.makeUnknown( masterDeviceDescriptor, MasterDeviceDescriptor::class, "roleName", masterDeviceDescriptor.roleName, unknownTypeName )

/**
 * Replace the type name of [registration] in this JSON string with [unknownTypeName].
 */
@ExperimentalSerializationApi
fun String.makeUnknown(
    registration: DeviceRegistration,
    unknownTypeName: String = "com.unknown.UnknownDeviceRegistration"
): String =
    this.makeUnknown( registration, DeviceRegistration::class, "deviceId", registration.deviceId, unknownTypeName )

/**
 * Replace the type name of [taskDescriptor] in this JSON string with [unknownTypeName].
 */
@ExperimentalSerializationApi
fun String.makeUnknown(
    taskDescriptor: TaskDescriptor<*>,
    unknownTypeName: String = "com.unknown.UnknownTaskDescriptor"
): String =
    this.makeUnknown( taskDescriptor, TaskDescriptor::class, "name", taskDescriptor.name, unknownTypeName )

/**
 * Replace the type name of the [samplingConfiguration] with the specified [key] set to [value] in this JSON string with [unknownTypeName].
 */
@ExperimentalSerializationApi
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
@ExperimentalSerializationApi
fun String.makeUnknown(
    trigger: Trigger<*>,
    key: String,
    value: String,
    unknownTypeName: String = "com.unknown.UnknownTrigger"
): String =
    this.makeUnknown( trigger, Trigger::class, key, value, unknownTypeName )

@ExperimentalSerializationApi
private fun <T : Any> String.makeUnknown(
    instance: T,
    klass: KClass<T>,
    key: String,
    value: String,
    unknownTypeName: String
): String
{
    // Get qualified type name.
    val serialModule = COMMON_SERIAL_MODULE + STUBS_SERIAL_MODULE
    val serializer = serialModule.getPolymorphic( klass, instance )
    val qualifiedName = serializer!!.descriptor.serialName

    // Construct regex to identify the object with the qualified name, and with the matching key/value pair set.
    // TODO: This regex uses negative lookahead to filter out JSON which contains multiple types with the same name.
    //       This is complex, and furthermore not 100% foolproof in rare cases (e.g., if the string is used not as a type name).
    //       Probably this should be rewritten with a JSON parser.
    val escapedQualifiedName = qualifiedName.replace( ".", "\\." )
    val objectRegex = Regex( "(\\{\"\\\$type\":\")($escapedQualifiedName)(\",(?!.*?$escapedQualifiedName.*?\"$key\":\"$value\").*?\"$key\":\"$value\".*?\\})" )

    // Replace type name with an unknown type name to mimic it is not available at runtime.
    val match = objectRegex.find( this )
    require( match != null && match.groups.count() == 4 ) { "Could not find the specified object in the serialized string." }
    return this.replace( objectRegex, "$1$unknownTypeName$3" )
}
