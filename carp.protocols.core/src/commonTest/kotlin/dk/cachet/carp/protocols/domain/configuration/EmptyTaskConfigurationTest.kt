package dk.cachet.carp.protocols.domain.configuration


/**
 * Test class for [EmptyTaskConfiguration].
 */
class EmptyTaskConfigurationTest : TaskConfigurationTest
{
    override fun createTaskConfiguration(): TaskConfiguration
    {
        return EmptyTaskConfiguration()
    }
}
