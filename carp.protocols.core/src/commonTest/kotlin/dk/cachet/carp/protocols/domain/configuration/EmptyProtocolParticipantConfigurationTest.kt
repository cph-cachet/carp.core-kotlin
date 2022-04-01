package dk.cachet.carp.protocols.domain.configuration


/**
 * Tests for [EmptyProtocolParticipantConfiguration].
 */
class EmptyProtocolParticipantConfigurationTest : ProtocolParticipantConfigurationTest
{
    override fun createParticipantConfiguration(): ProtocolParticipantConfiguration =
        EmptyProtocolParticipantConfiguration()
}
