package bhrp.studyprotocol.domain.tasks


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