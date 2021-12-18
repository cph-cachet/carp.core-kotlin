package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.datetime.Instant


/**
 * A composition root used to initialize [StudyProtocol] with concrete implementations of its interfaces.
 */
abstract class StudyProtocolComposition internal constructor(
    protected val deviceConfiguration: DeviceConfiguration,
    protected val taskConfiguration: TaskConfiguration,
    protected val participantDataConfiguration: ParticipantDataConfiguration,
    id: UUID,
    createdOn: Instant
) : DeviceConfiguration by deviceConfiguration,
    TaskConfiguration by taskConfiguration,
    ParticipantDataConfiguration by participantDataConfiguration,
    AggregateRoot<StudyProtocol, StudyProtocolSnapshot, StudyProtocol.Event>( id, createdOn )
