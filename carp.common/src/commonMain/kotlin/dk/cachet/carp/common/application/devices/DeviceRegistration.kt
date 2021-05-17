package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * A [DeviceRegistration] uniquely identifies a [DeviceDescriptor] once it is deployed in a study,
 * contains the necessary details for the client on how to connect to the device,
 * and stores device specifications which may be relevant to the researcher when interpreting collection data,
 * such as brand/model name and operating system version.
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
abstract class DeviceRegistration
{
    /**
     * An ID for the device, used to disambiguate between devices of the same type, as provided by the device itself.
     * It is up to specific types of devices to guarantee uniqueness across all devices of the same type.
     *
     * TODO: This might be useful for potential optimizations later (e.g., prevent pulling in data from the same source more than once), but for now is ignored.
     */
    abstract val deviceId: String

    val registrationCreationDate: DateTime = DateTime.now()
}


/**
 * A helper class to configure and construct immutable [DeviceRegistration] classes.
 *
 * TODO: This and extending classes are never expected to be serialized,
 *       but need to be [Serializable] since they are specified as generic type parameter on [DeviceDescriptor].
 */
@Serializable( NotSerializable::class )
@DeviceRegistrationBuilderDsl
interface DeviceRegistrationBuilder<T : DeviceRegistration>
{
    /**
     * Build the immutable [DeviceRegistration] using the current configuration of this [DeviceRegistrationBuilder].
     */
    fun build(): T
}

/**
 * Should be applied to all builders participating in building [DeviceRegistration]s to prevent misuse of internal DSL.
 * For more information: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html
 */
@DslMarker
annotation class DeviceRegistrationBuilderDsl
