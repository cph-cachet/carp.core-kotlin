package carp.protocols.domain.deployment

import carp.protocols.domain.StudyProtocol
import carp.protocols.domain.devices.DeviceDescriptor
import carp.protocols.domain.devices.MasterDeviceDescriptor


/**
 * Evaluates whether a [StudyProtocol] contains any devices which are never used as the source or target of triggers, or to relay data.
 *
 * Devices which are never used as part of triggers or to relay data serve no purpose in the [StudyProtocol].
 */
class UnusedDevicesWarning internal constructor() : DeploymentWarning
{
    override val description =
        "The study protocol contains devices which are never used as the source or target of triggers, or to relay data (master device). " +
        "These devices thus serve no purpose as part of the specified study protocol."

    override fun isIssuePresent( protocol: StudyProtocol ): Boolean
    {
        return getUnusedDevices( protocol ).any()
    }

    fun getUnusedDevices( protocol: StudyProtocol ): Set<DeviceDescriptor>
    {
        // Get all devices used in triggers.
        val usedDevices: Set<DeviceDescriptor> = protocol.triggers.flatMap { trigger ->
            val usedInTrigger = protocol.getTriggeredTasks( trigger ).map { it.device }.toMutableList()
            usedInTrigger.add( protocol.devices.single { trigger.sourceDeviceRoleName == it.roleName } )
            usedInTrigger
        }.toSet()

        val unusedDevices = protocol.devices.minus( usedDevices )

        // Master devices which are not used in triggers but have connected devices used in triggers are still used to relay data.
        val relayingMasterDevices = unusedDevices.filterIsInstance<MasterDeviceDescriptor>().filter {
            protocol.getConnectedDevices( it, true ).any { usedDevices.contains( it ) }
        }

        return unusedDevices.minus( relayingMasterDevices )
    }
}