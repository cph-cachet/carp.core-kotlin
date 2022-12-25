package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.triggers.ElapsedTimeTrigger
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.time.Duration


/**
 * A device which aggregates, synchronizes, and optionally uploads incoming data received from one or more connected devices (potentially just itself).
 * Typically, a desktop computer, smartphone, or web server.
 */
@Serializable
@Polymorphic
abstract class PrimaryDeviceConfiguration<
    TRegistration : DeviceRegistration,
    out TBuilder : DeviceRegistrationBuilder<TRegistration>
> : DeviceConfiguration<TRegistration, TBuilder>()
{
    // This property is only here for (de)serialization purposes.
    // For unknown types we need to know whether to treat them as primary devices or not (in the case of 'DeviceConfiguration' collections).
    @Required
    internal val isPrimaryDevice: Boolean = true

    /**
     * Get a trigger which fires immediately at the start of a study deployment.
     */
    fun atStartOfStudy(): ElapsedTimeTrigger = ElapsedTimeTrigger( this, Duration.ZERO )
}

typealias AnyPrimaryDeviceConfiguration = PrimaryDeviceConfiguration<*, *>
