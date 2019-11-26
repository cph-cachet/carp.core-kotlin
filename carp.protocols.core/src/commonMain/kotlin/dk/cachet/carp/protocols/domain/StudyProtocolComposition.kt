package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*


/**
 * A composition root used to initialize [StudyProtocol] with concrete implementations of its interfaces.
 */
abstract class StudyProtocolComposition internal constructor(
    protected val deviceConfiguration: DeviceConfiguration,
    protected val taskConfiguration: TaskConfiguration ) :
    DeviceConfiguration by deviceConfiguration,
    TaskConfiguration by taskConfiguration