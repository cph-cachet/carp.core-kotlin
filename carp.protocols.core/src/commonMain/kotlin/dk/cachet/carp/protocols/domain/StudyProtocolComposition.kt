package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.ddd.AggregateRoot
import dk.cachet.carp.protocols.domain.devices.DeviceConfiguration
import dk.cachet.carp.protocols.domain.tasks.TaskConfiguration


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
