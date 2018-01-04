package bhrp.studyprotocol.domain


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers).
 */
fun createEmptyProtocol(): StudyProtocol
{
    return StudyProtocol( ProtocolOwner(), "Test protocol" )
}