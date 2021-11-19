package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Evaluates whether a [StudyProtocol] contains only optional master devices.
 *
 * If all master devices are optional, this means a deployment could "start" without any devices or participants.
 */
class OnlyOptionalDevicesWarning : DeploymentWarning
{
    override val description: String =
        "The study protocol only contains optional master devices. " +
        "This implies that a deployment could 'start' without any devices or participants, indicating a problem."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean =
        protocol.masterDevices.all { it.isOptional }
}
