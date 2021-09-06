package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.studies.application.users.StudyOwner
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Create a 'complex' study for testing purposes.
 */
fun createComplexStudy(): Study
{
    val owner = StudyOwner()
    val invitation = StudyInvitation( "Some study" )
    val study = Study( owner, "Test", "Description", invitation )

    // Specify protocol.
    val protocol = StudyProtocol( ProtocolOwner(), "Test protocol" )
    protocol.addMasterDevice( Smartphone( "User's phone" ) ) // Needed to make the protocol deployable.
    study.protocolSnapshot = protocol.getSnapshot()

    // Go live.
    study.goLive()

    return study
}
