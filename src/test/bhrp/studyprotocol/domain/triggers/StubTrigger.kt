package bhrp.studyprotocol.domain.triggers

import bhrp.studyprotocol.domain.devices.DeviceDescriptor


data class StubTrigger( override val sourceDevice: DeviceDescriptor, val uniqueProperty: String = "Unique" ) : Trigger()