package carp.protocols.domain.triggers

import carp.protocols.domain.devices.DeviceDescriptor


data class StubTrigger( override val sourceDevice: DeviceDescriptor, val uniqueProperty: String = "Unique" ) : Trigger()