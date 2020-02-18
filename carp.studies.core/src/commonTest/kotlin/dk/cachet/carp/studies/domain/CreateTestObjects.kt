package dk.cachet.carp.studies.domain

import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Create a 'complex' study for testing purposes.
 */
fun createComplexStudy(): Study
{
    val owner = StudyOwner()
    val invitation = StudyInvitation.empty()

    return Study( owner, "Test", invitation )
}
