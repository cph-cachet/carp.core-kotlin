package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Evaluates whether a [StudyProtocol] contains at least one primary device.
 *
 * Without a primary device, no data can be collected.
 */
class NoPrimaryDeviceError internal constructor() : DeploymentError
{
    override val description: String =
        "At least one primary device needs to be specified in a study protocol. " +
        "Without a primary device, no data can be collected."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean = !protocol.primaryDevices.any()
}
