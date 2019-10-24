package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.UUID


/**
 * Uniquely identifies a participant as part of a particular [Study].
 */
data class Participant( val studyId: UUID, val id: UUID = UUID.randomUUID() )