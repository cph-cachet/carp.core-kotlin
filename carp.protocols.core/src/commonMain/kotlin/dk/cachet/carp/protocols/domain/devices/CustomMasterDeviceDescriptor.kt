package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass


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
     * For unknown types, it cannot be determined whether or not a given configuration is valid.
     */
    override fun isValidConfiguration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}