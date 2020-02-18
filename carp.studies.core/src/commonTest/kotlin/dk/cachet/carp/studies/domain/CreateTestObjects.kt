package dk.cachet.carp.studies.domain

import dk.cachet.carp.deployment.domain.users.StudyInvitation


/**
 * Create a 'complex' study for testing purposes.
 */
fun createComplexStudy(): Study
{
    val owner = StudyOwner()
    val invitation = StudyInvitation.empty()

    return Study( owner, "Test", invitation )
}
