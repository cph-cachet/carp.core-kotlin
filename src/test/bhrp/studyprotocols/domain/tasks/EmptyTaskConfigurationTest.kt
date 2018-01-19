package bhrp.studyprotocols.domain.tasks


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