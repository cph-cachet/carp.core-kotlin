package bhrp.studyprotocol.domain

import bhrp.studyprotocol.domain.devices.*
import bhrp.studyprotocol.domain.tasks.*


/**
 * A composition root used to initialize [StudyProtocol] with concrete implementations of its interfaces.
 */
abstract class StudyProtocolComposition internal constructor(
    protected val _deviceConfiguration: DeviceConfiguration,
    protected val _taskConfiguration: TaskConfiguration ) :
    DeviceConfiguration by _deviceConfiguration,
    TaskConfiguration by _taskConfiguration