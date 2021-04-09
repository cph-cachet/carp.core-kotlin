package dk.cachet.carp.common.domain

import dk.cachet.carp.common.application.StudyProtocolSnapshot
import dk.cachet.carp.common.domain.devices.DeviceConfiguration
import dk.cachet.carp.common.domain.tasks.TaskConfiguration


/**
 * A composition root used to initialize [StudyProtocol] with concrete implementations of its interfaces.
 */
abstract class StudyProtocolComposition internal constructor(
    protected val deviceConfiguration: DeviceConfiguration,
    protected val taskConfiguration: TaskConfiguration,
    protected val participantDataConfiguration: ParticipantDataConfiguration
) : DeviceConfiguration by deviceConfiguration,
    TaskConfiguration by taskConfiguration,
    ParticipantDataConfiguration by participantDataConfiguration,
    AggregateRoot<StudyProtocol, StudyProtocolSnapshot, StudyProtocol.Event>()
