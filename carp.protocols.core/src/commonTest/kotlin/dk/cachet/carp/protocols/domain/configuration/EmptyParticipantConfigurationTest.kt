package dk.cachet.carp.protocols.domain.configuration


/**
 * Tests for [EmptyParticipantConfiguration].
 */
class EmptyParticipantConfigurationTest : ParticipantConfigurationTest
{
    override fun createParticipantConfiguration(): ParticipantConfiguration =
        EmptyParticipantConfiguration()
}
