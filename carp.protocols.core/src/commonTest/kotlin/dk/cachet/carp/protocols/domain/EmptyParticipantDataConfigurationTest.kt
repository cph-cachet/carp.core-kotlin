package dk.cachet.carp.protocols.domain


/**
 * Tests for [EmptyParticipantDataConfiguration].
 */
class EmptyParticipantDataConfigurationTest : ParticipantDataConfigurationTest
{
    override fun createParticipantDataConfiguration(): ParticipantDataConfiguration =
        EmptyParticipantDataConfiguration()
}
