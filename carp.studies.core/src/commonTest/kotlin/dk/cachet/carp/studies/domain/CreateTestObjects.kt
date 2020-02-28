package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Create a 'complex' study for testing purposes.
 */
fun createComplexStudy(): Study
{
    val owner = StudyOwner()
    val invitation = StudyInvitation.empty()
    val study = Study( owner, "Test", invitation )

    // Specify protocol.
    val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
    protocol.addMasterDevice( Smartphone( "User's phone" ) ) // Needed to make the protocol deployable.
    study.protocolSnapshot = protocol.getSnapshot()

    // Go live.
    study.goLive()

    // Add a participation.
    val participation = DeanonymizedParticipation( UUID.randomUUID(), Participation( UUID.randomUUID() ) )
    study.addParticipation( participation )

    return study
}
