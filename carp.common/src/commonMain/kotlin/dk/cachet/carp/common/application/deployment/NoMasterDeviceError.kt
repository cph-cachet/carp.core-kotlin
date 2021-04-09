package dk.cachet.carp.common.application.deployment

import dk.cachet.carp.common.domain.StudyProtocol


/**
 * Evaluates whether a [StudyProtocol] contains at least one master device.
 *
 * Without a master device, no data can be collected.
 */
class NoMasterDeviceError internal constructor() : DeploymentError
{
    override val description: String =
        "At least one master device needs to be specified in a study protocol. " +
        "Without a master device, no data can be collected."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean = !protocol.masterDevices.any()
}
