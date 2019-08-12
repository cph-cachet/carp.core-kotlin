package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass


/**
 * A wrapper used to load extending types from [DeviceDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomDeviceDescriptor( override val className: String, override val jsonSource: String, val serializer: Json )
    : DeviceDescriptor<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(), UnknownPolymorphicWrapper
{
    override val roleName: String

    init
    {
        val json = serializer.parseJson( jsonSource ) as JsonObject

        val roleNameField = AnyDeviceDescriptor::roleName.name
        require( json.containsKey( roleNameField ) ) { "No '$roleNameField' defined." }
        roleName = json[ roleNameField ]!!.content
    }

    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration>
        = throw UnsupportedOperationException( "The concrete type of this device is not known. Therefore, it is unknown which registration builder is required." )

    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class

    /**
     * For unknown types, it cannot be determined whether or not a given registration is valid.
     */
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}

/**
 * A wrapper used to load extending types from [MasterDeviceDescriptor] serialized as JSON which are unknown at runtime.
 */
data class CustomMasterDeviceDescriptor( override val className: String, override val jsonSource: String, val serializer: Json )
    : MasterDeviceDescriptor<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(), UnknownPolymorphicWrapper
{
    override val roleName: String

    init
    {
        val json = serializer.parseJson( jsonSource ) as JsonObject

        val roleNameField = AnyMasterDeviceDescriptor::roleName.name
        require( json.containsKey( roleNameField ) ) { "No '$roleNameField' defined." }
        roleName = json[ roleNameField ]!!.content
    }

    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration>
        = throw UnsupportedOperationException( "The concrete type of this device is not known. Therefore, it is unknown which registration builder is required." )

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
        val jsonObject = serializer.parseJson( json ) as JsonObject
        val isMasterDevice = jsonObject.containsKey( AnyMasterDeviceDescriptor::isMasterDevice.name )
        return if ( isMasterDevice )
            CustomMasterDeviceDescriptor( className, json, serializer )
        else CustomDeviceDescriptor( className, json, serializer )
    }
}

/**
 * Custom serializer for a list of [DeviceDescriptor]s which enables deserializing types that are unknown at runtime, yet extend from [DeviceDescriptor].
 */
object DevicesSerializer : KSerializer<List<AnyDeviceDescriptor>> by ArrayListSerializer( DeviceDescriptorSerializer )

/**
 * Custom serializer for a set of [DeviceDescriptor]s which enables deserializing types that are unknown at runtime, yet extend from [DeviceDescriptor].
 */
object DevicesSetSerializer : KSerializer<Set<AnyDeviceDescriptor>> by HashSetSerializer( DeviceDescriptorSerializer )

/**
 * Custom serializer for a list of [MasterDeviceDescriptor]s which enables deserializing types that are unknown at runtime, yet extend from [MasterDeviceDescriptor].
 */
@Suppress( "RemoveExplicitTypeArguments" ) // Removing this fails compilation. Might be a bug in the analyzer.
object MasterDevicesSerializer : KSerializer<List<AnyMasterDeviceDescriptor>> by ArrayListSerializer<AnyMasterDeviceDescriptor>(
    createUnknownPolymorphicSerializer { className, json, serializer -> CustomMasterDeviceDescriptor( className, json, serializer ) }
)


/**
 * A wrapper used to load extending types from [DeviceRegistration] serialized as JSON which are unknown at runtime.
 */
data class CustomDeviceRegistration( override val className: String, override val jsonSource: String, val serializer: Json )
    : DeviceRegistration(), UnknownPolymorphicWrapper
{
    override val deviceId: String

    init
    {
        val json = serializer.parseJson( jsonSource ) as JsonObject

        val deviceIdField = DeviceRegistration::deviceId.name
        require( json.containsKey( deviceIdField ) ) { "No '$deviceIdField' defined." }
        deviceId = json[ deviceIdField ]!!.content
    }
}

/**
 * Custom serializer for a [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
object DeviceRegistrationSerializer : KSerializer<DeviceRegistration>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomDeviceRegistration( className, json, serializer ) } )