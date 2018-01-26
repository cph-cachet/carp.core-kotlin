package carp.protocols.domain.triggers

import carp.protocols.domain.common.Immutable
import carp.protocols.domain.devices.DeviceDescriptor
import carp.protocols.domain.notImmutableErrorFor


/**
 * Any condition on a device ([DeviceDescriptor]) which is (or can be) initiated at a certain point in time when the condition applies.
 * The condition can either be time-bound, based on data streams, initiated by a user of the platform, or a combination of these.
 */
abstract class Trigger : Immutable( notImmutableErrorFor( Trigger::class ) )
{
    /**
     * The device from which the trigger originates.
     */
    abstract val sourceDevice: DeviceDescriptor
}