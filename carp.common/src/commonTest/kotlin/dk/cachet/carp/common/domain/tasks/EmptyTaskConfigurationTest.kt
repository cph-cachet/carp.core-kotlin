package dk.cachet.carp.common.domain.tasks


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
