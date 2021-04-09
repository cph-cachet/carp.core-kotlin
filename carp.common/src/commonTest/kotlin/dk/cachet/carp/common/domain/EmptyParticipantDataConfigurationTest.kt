package dk.cachet.carp.common.domain


/**
 * Tests for [EmptyParticipantDataConfiguration].
 */
class EmptyParticipantDataConfigurationTest : ParticipantDataConfigurationTest
{
    override fun createParticipantDataConfiguration(): ParticipantDataConfiguration =
        EmptyParticipantDataConfiguration()
}
