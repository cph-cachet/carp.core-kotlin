package bhrp.studyprotocol.domain.devices

import bhrp.studyprotocol.domain.common.Immutable
import bhrp.studyprotocol.domain.InvalidConfigurationError


/**
 * Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone
 * that collects data which can be incorporated into the platform after it has been processed by a master device (potentially itself).
 * Optionally, a device can present output and receive user input.
 *
 * TODO: Does this also allow specifying dynamic devices? E.g., 'closest smartphone'. Perhaps a 'DeviceSelector'?
 */
abstract class DeviceDescriptor
    : Immutable( InvalidConfigurationError( "Implementations of DeviceDescriptor should be data classes and may not contain any mutable properties." ) )
{
    /**
     * A name which describes how the device participates within the study protocol; it's 'role'.
     * E.g., "Patient's phone"
     */
    abstract val roleName: String
}