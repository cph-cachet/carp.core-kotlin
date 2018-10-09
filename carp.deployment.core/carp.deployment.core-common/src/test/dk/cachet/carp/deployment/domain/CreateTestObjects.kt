package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.protocols.domain.*


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers).
 */
fun createEmptyProtocol(): StudyProtocol
{
    val alwaysSameOwner = ProtocolOwner( UUID( "f3f4d91b-56b5-4117-bb98-7e2923cb2223" ) )
    return StudyProtocol( alwaysSameOwner, "Test protocol" )
}

/**
 * Creates a study protocol with nothing more than a single master device.
 */
fun createSingleMasterDeviceProtocol(): StudyProtocol
{
    val protocol = createEmptyProtocol()
    protocol.addMasterDevice( StubMasterDeviceDescriptor() )
    return protocol
}