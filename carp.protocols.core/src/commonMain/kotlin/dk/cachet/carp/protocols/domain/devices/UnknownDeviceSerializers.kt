package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.createUnknownPolymorphicSerializer
import dk.cachet.carp.common.serialization.UnknownPolymorphicSerializer
import dk.cachet.carp.common.serialization.UnknownPolymorphicWrapper
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.data.SamplingConfiguration
import dk.cachet.carp.protocols.domain.data.SamplingConfigurationSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>

    init
    {
        val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

        val roleNameField = AnyDeviceDescriptor::roleName.name
        require( roleNameField in json.keys ) { "No '$roleNameField' defined." }
        roleName = json[ roleNameField ]!!.jsonPrimitive.content

        val samplingConfigurationField = AnyDeviceDescriptor::samplingConfiguration.name
        samplingConfiguration =
            if ( samplingConfigurationField in json.keys )
            {
                val configurationJson: String = json[ samplingConfigurationField ]!!.jsonObject.toString()
                val configurationSerializer = MapSerializer( DataType.serializer(), SamplingConfigurationSerializer )
                serializer.decodeFromString( configurationSerializer, configurationJson )
            }
            else emptyMap()
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
    override val samplingConfiguration: Map<DataType, SamplingConfiguration>

    init
    {
        val json = serializer.parseToJsonElement( jsonSource ) as JsonObject

        val roleNameField = AnyMasterDeviceDescriptor::roleName.name
        require( roleNameField in json.keys ) { "No '$roleNameField' defined." }
        roleName = json[ roleNameField ]!!.jsonPrimitive.content

        val samplingConfigurationField = AnyDeviceDescriptor::samplingConfiguration.name
        samplingConfiguration =
            if ( samplingConfigurationField in json.keys )
            {
                val configurationJson: String = json[ samplingConfigurationField ]!!.jsonObject.toString()
                val configurationSerializer = MapSerializer( DataType.serializer(), SamplingConfigurationSerializer )
                serializer.decodeFromString( configurationSerializer, configurationJson )
            }
            else emptyMap()
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
