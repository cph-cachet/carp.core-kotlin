package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID


/**
 * Uniquely identifies the person or group that created a [Study].
 */
data class StudyOwner( val id: UUID = UUID.randomUUID() )