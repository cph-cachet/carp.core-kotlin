package dk.cachet.carp.protocols.domain.configuration


/**
 * Tests for [EmptyParticipantDataConfiguration].
 */
class EmptyParticipantDataConfigurationTest : ParticipantDataConfigurationTest
{
    override fun createParticipantDataConfiguration(): ParticipantDataConfiguration =
        EmptyParticipantDataConfiguration()
}
