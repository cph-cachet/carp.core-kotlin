package dk.cachet.carp.protocols.domain.devices

import dk.cachet.carp.protocols.domain.triggers.StartOfStudyTrigger
import kotlinx.serialization.Serializable


/**
 * A device which aggregates, synchronizes, and optionally uploads incoming data received from one or more connected devices (potentially just itself).
 * Typically, a desktop computer, smartphone, or web server.
 */
@Serializable
abstract class MasterDeviceDescriptor : DeviceDescriptor()
{
    // This property is only here for (de)serialization purposes.
    // For unknown types we need to know whether to treat them as master devices or not (in the case of 'DeviceDescriptor' collections).
    internal val isMasterDevice: Boolean = true

    /**
     * Get a trigger which is initialized immediately at the start of a study and runs indefinitely.
     */
    fun atStartOfStudy(): StartOfStudyTrigger = StartOfStudyTrigger( this )
}