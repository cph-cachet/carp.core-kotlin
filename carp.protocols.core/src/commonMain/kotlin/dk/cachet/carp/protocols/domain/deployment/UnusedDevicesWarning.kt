package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.StudyProtocol


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


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean = getUnusedDevices( protocol ).any()

    fun getUnusedDevices( protocol: StudyProtocol ): Set<AnyDeviceDescriptor>
    {
        // Get all devices used in triggers.
        val usedDevices: Set<AnyDeviceDescriptor> = protocol.triggers.flatMap { trigger ->
            val usedInTrigger =
                protocol.getTriggeredTasks( trigger ).map { it.targetDevice }.toMutableList()
            usedInTrigger.add( protocol.devices.single { trigger.sourceDeviceRoleName == it.roleName } )
            usedInTrigger
        }.toSet()

        val unusedDevices = protocol.devices.minus( usedDevices )

        // Master devices which are not used in triggers but have connected devices used in triggers are still used to relay data.
        val relayingMasterDevices = unusedDevices.filterIsInstance<AnyMasterDeviceDescriptor>().filter { device ->
            protocol.getConnectedDevices( device, true ).any { usedDevices.contains( it ) }
        }

        return unusedDevices.minus( relayingMasterDevices )
    }
}
