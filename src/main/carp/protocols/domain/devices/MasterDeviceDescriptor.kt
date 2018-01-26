package carp.protocols.domain.devices


/**
 * A device which aggregates, synchronizes, and optionally uploads incoming data received from one or more connected devices (potentially just itself).
 * Typically, a desktop computer, smartphone, or web server.
 */
abstract class MasterDeviceDescriptor : DeviceDescriptor()