package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Evaluates whether a [StudyProtocol] contains only optional primary devices.
 *
 * If all primary devices are optional, this means a deployment could "start" without any devices or participants.
 */
class OnlyOptionalDevicesWarning : DeploymentWarning
{
    override val description: String =
        "The study protocol only contains optional primary devices. " +
        "This implies that a deployment could 'start' without any devices or participants, indicating a problem."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean =
        protocol.primaryDevices.all { it.isOptional }
}
