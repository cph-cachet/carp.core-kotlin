package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.common.serialization.createUnknownPolymorphicSerializer
import dk.cachet.carp.common.serialization.UnknownPolymorphicSerializer
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.sampling.SamplingConfiguration
import dk.cachet.carp.protocols.domain.sampling.SamplingConfigurationSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass


/**
 * A wrapper used to load extending types from [DeviceDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomDeviceDescriptor( override val className: String, override val jsonSource: String, val serializer: Json ) :
    DeviceDescriptor<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(), UnknownPolymorphicWrapper
{
    override val roleName: String
    override val isOptional: Boolean
    override val supportedDataTypes: Set<DataType>
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>

    init
    {
        val parsed = parseDeviceDescriptorFields( jsonSource, serializer )
        roleName = parsed.roleName
        isOptional = parsed.isOptional
        supportedDataTypes = parsed.supportedDataTypes
        samplingConfiguration = parsed.samplingConfiguration
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
data class CustomMasterDeviceDescriptor( override val className: String, override val jsonSource: String, val serializer: Json ) :
    MasterDeviceDescriptor<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(), UnknownPolymorphicWrapper
{
    override val roleName: String
    override val isOptional: Boolean
    override val supportedDataTypes: Set<DataType>
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>

    init
    {
        val parsed = parseDeviceDescriptorFields( jsonSource, serializer )
        roleName = parsed.roleName
        isOptional = parsed.isOptional
        supportedDataTypes = parsed.supportedDataTypes
        samplingConfiguration = parsed.samplingConfiguration
    }

    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration> =
        throw UnsupportedOperationException( "The concrete type of this device is not known. Therefore, it is unknown which registration builder is required." )

    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class

    /**
     * For unknown types, it cannot be determined whether or not a given registration is valid.
     */
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}


private data class DeviceDescriptorFields(
    val roleName: String,
    val isOptional: Boolean,
    val supportedDataTypes: Set<DataType>,
    val samplingConfiguration: Map<DataType, SamplingConfiguration>
)

private fun parseDeviceDescriptorFields( jsonSource: String, serializer: Json ): DeviceDescriptorFields
{
    val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

    val roleNameField = AnyDeviceDescriptor::roleName.name
    require( roleNameField in json.keys ) { "No '$roleNameField' defined." }
    val roleName = json[ roleNameField ]!!.jsonPrimitive.content

    val isOptionalField = AnyDeviceDescriptor::isOptional.name
    val isOptional =
        if ( isOptionalField in json.keys ) json[ isOptionalField ]!!.jsonPrimitive.boolean
        else false

    val supportedDataTypesField = AnyDeviceDescriptor::supportedDataTypes.name
    val supportedDataTypes =
        if ( supportedDataTypesField in json.keys )
        {
            val supportedTypesJson = json[ supportedDataTypesField ]!!.jsonArray.toString()
            val supportedTypesSerializer = SetSerializer( DataType.serializer() )
            serializer.decodeFromString( supportedTypesSerializer, supportedTypesJson )
        }
        else emptySet()

    val samplingConfigurationField = AnyDeviceDescriptor::samplingConfiguration.name
    val samplingConfiguration =
        if ( samplingConfigurationField in json.keys )
        {
            val configurationJson: String = json[ samplingConfigurationField ]!!.jsonObject.toString()
            val configurationSerializer = MapSerializer( DataType.serializer(), SamplingConfigurationSerializer )
            serializer.decodeFromString( configurationSerializer, configurationJson )
        }
        else emptyMap()

    return DeviceDescriptorFields( roleName, isOptional, supportedDataTypes, samplingConfiguration )
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
data class CustomDeviceRegistration( override val className: String, override val jsonSource: String, val serializer: Json ) :
    DeviceRegistration(), UnknownPolymorphicWrapper
{
    override val deviceId: String

    init
    {
        val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

        val deviceIdField = DeviceRegistration::deviceId.name
        require( json.containsKey( deviceIdField ) ) { "No '$deviceIdField' defined." }
        deviceId = json[ deviceIdField ]!!.jsonPrimitive.content
    }
}

/**
 * Custom serializer for a [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
object DeviceRegistrationSerializer : KSerializer<DeviceRegistration>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomDeviceRegistration( className, json, serializer ) } )
