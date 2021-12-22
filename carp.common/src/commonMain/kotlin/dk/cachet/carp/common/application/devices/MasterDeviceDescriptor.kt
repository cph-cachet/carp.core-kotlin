package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.triggers.ElapsedTimeTrigger
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Duration


/**
 * A device which aggregates, synchronizes, and optionally uploads incoming data received from one or more connected devices (potentially just itself).
 * Typically, a desktop computer, smartphone, or web server.
 */
@Serializable
@Polymorphic
@JsExport
abstract class MasterDeviceDescriptor<TRegistration : DeviceRegistration, out TBuilder : DeviceRegistrationBuilder<TRegistration>> :
    DeviceDescriptor<TRegistration, TBuilder>()
{
    // This property is only here for (de)serialization purposes.
    // For unknown types we need to know whether to treat them as master devices or not (in the case of 'DeviceDescriptor' collections).
    @Required
    internal val isMasterDevice: Boolean = true

    /**
     * Get a trigger which fires immediately at the start of a study deployment.
     */
    fun atStartOfStudy(): ElapsedTimeTrigger = ElapsedTimeTrigger( this, Duration.ZERO )
}

typealias AnyMasterDeviceDescriptor = MasterDeviceDescriptor<*, *>
