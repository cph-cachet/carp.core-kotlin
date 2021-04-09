package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistrationBuilder
import dk.cachet.carp.common.application.devices.MasterDeviceDescriptor
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass


/**
 * A wrapper used to load extending types from [DeviceDescriptor] serialized as JSON which are unknown at runtime.
 */
@Serializable( DeviceDescriptorSerializer::class )
data class CustomDeviceDescriptor( override val className: String, override val jsonSource: String, val serializer: Json ) :
    DeviceDescriptor<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(), UnknownPolymorphicWrapper
{
    override val roleName: String
    override val supportedDataTypes: Set<DataType>
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        roleName = baseMembers.roleName
        supportedDataTypes = baseMembers.supportedDataTypes
        samplingConfiguration = baseMembers.samplingConfiguration
    }

    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration> =
        throw UnsupportedOperationException( "The concrete type of this device is not known. Therefore, it is unknown which registration builder is required." )

    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class

    /**
     * For unknown types, it cannot be determined whether or not a given registration is valid.
     */
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}


/**
 * A wrapper used to load extending types from [MasterDeviceDescriptor] serialized as JSON which are unknown at runtime.
 */
@Serializable( MasterDeviceDescriptorSerializer::class )
data class CustomMasterDeviceDescriptor( override val className: String, override val jsonSource: String, val serializer: Json ) :
    MasterDeviceDescriptor<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(), UnknownPolymorphicWrapper
{
    override val roleName: String
    override val supportedDataTypes: Set<DataType>
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        roleName = baseMembers.roleName
        supportedDataTypes = baseMembers.supportedDataTypes
        samplingConfiguration = baseMembers.samplingConfiguration
    }

    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration> =
        throw UnsupportedOperationException( "The concrete type of this device is not known. Therefore, it is unknown which registration builder is required." )

    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class

    /**
     * For unknown types, it cannot be determined whether or not a given registration is valid.
     */
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}


@Serializable
private data class BaseMembers(
    override val roleName: String,
    override val supportedDataTypes: Set<DataType>,
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>
) : DeviceDescriptor<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>()
{
    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration> =
        throw UnsupportedOperationException()
    override fun getRegistrationClass(): KClass<DeviceRegistration> =
        throw UnsupportedOperationException()
    override fun isValidConfiguration( registration: DeviceRegistration ): Trilean =
        throw UnsupportedOperationException()
}


/**
 * Custom serializer for [DeviceDescriptor] which enables deserializing types that are unknown at runtime, yet extend from [DeviceDescriptor].
 */
object DeviceDescriptorSerializer : UnknownPolymorphicSerializer<AnyDeviceDescriptor, AnyDeviceDescriptor>( DeviceDescriptor::class, DeviceDescriptor::class, false )
{
    override fun createWrapper( className: String, json: String, serializer: Json ): AnyDeviceDescriptor
    {
        val jsonObject = serializer.parseToJsonElement( json ) as JsonObject
        val isMasterDevice = jsonObject.containsKey( AnyMasterDeviceDescriptor::isMasterDevice.name )

        return if ( isMasterDevice )
        {
            CustomMasterDeviceDescriptor( className, json, serializer )
        }
        else
        {
            CustomDeviceDescriptor( className, json, serializer )
        }
    }
}

/**
 * Custom serializer for [MasterDeviceDescriptor] which enables deserializing types that are unknown at runtime, yet extend from [MasterDeviceDescriptor].
 */
object MasterDeviceDescriptorSerializer : KSerializer<AnyMasterDeviceDescriptor>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomMasterDeviceDescriptor( className, json, serializer ) } )


/**
 * A wrapper used to load extending types from [DeviceRegistration] serialized as JSON which are unknown at runtime.
 */
@Serializable( DeviceRegistrationSerializer::class )
data class CustomDeviceRegistration( override val className: String, override val jsonSource: String, val serializer: Json ) :
    DeviceRegistration(), UnknownPolymorphicWrapper
{
    @Serializable
    private data class BaseMembers( override val deviceId: String ) : DeviceRegistration()

    override val deviceId: String

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        deviceId = baseMembers.deviceId
    }
}

/**
 * Custom serializer for a [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
object DeviceRegistrationSerializer : KSerializer<DeviceRegistration>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomDeviceRegistration( className, json, serializer ) } )
