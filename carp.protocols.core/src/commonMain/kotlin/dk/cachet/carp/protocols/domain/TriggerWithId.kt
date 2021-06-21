package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.triggers.Trigger


/**
 * A [trigger] with an [id] as assigned to in a [StudyProtocol].
 */
data class TriggerWithId( val id: Int, val trigger: Trigger )
