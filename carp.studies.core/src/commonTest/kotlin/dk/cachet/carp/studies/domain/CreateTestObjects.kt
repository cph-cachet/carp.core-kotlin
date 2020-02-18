package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.StudyInvitation


/**
 * Create a study with a couple of participants added.
 */
fun createComplexStudy(): Study
{
    val owner = StudyOwner()
    val invitation = StudyInvitation.empty()
    val study = Study( owner, "Test", invitation )

    study.includeParticipant( UUID.randomUUID() )
    study.includeParticipant( UUID.randomUUID() )

    return study
}
