package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.*


/**
 * Uniquely identifies an account with associated identities and the studies it participates in.
 * TODO: For now only an email identity is implemented. Simple 'username' support might also need to be provided.
 */
data class Account(
    val emailAddress: EmailAddress,
    /**
     * The set of studies this account participates in as a participant.
     */
    val studyParticipations: Set<Participant> = setOf(),
    val id: UUID = UUID.randomUUID() )