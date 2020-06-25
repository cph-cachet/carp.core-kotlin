package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.serialization.NotSerializable
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


/**
 * A [DeviceRegistration] configures a [DeviceDescriptor] as part of the deployment of a [StudyProtocol].
 */
@Serializable
@Polymorphic
abstract class DeviceRegistration : Immutable( notImmutableErrorFor( DeviceRegistration::class ) )
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
