package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import org.junit.Assert.*


/**
 * Base class with tests for [TaskConfiguration] which can be used to test extending types.
 */
interface TaskConfigurationTest
{
    /**
     * Called for each test to create a task configuration to run tests on.
     */
    fun createTaskConfiguration(): TaskConfiguration


    @Test
    fun `addTask succeeds`()
    {
        val configuration = createTaskConfiguration()

        val task = StubTaskDescriptor()
        val isAdded: Boolean = configuration.addTask( task )
        assertTrue( isAdded )
        assertTrue( configuration.tasks.contains( task ) )
    }

    @Test
    fun `removeTask succeeds`()
    {
        val configuration = createTaskConfiguration()
        val task = StubTaskDescriptor()
        configuration.addTask( task )

        val isRemoved: Boolean = configuration.removeTask( task )
        assertTrue( isRemoved )
        assertFalse( configuration.tasks.contains( task ) )
    }

    @Test
    fun `removeTask returns false when task not present`()
    {
        val configuration = createTaskConfiguration()
        val task = StubTaskDescriptor()

        val isRemoved: Boolean = configuration.removeTask( task )
        assertFalse( isRemoved )
    }

    @Test
    fun `addTask multiple times only adds first time`()
    {
        val configuration = createTaskConfiguration()
        val task = StubTaskDescriptor()
        configuration.addTask( task )

        val isAdded: Boolean = configuration.addTask( task )
        assertFalse( isAdded )
        assertEquals( 1, configuration.tasks.count() )
    }

    @Test
    fun `do not allow duplicate names for tasks`()
    {
        val configuration = createTaskConfiguration()
        configuration.addTask( StubTaskDescriptor( "Unique name" ) )
        configuration.addTask( StubTaskDescriptor( "Duplicate name" ) )

        // Adding an additional task with duplicate name should fail.
        assertFailsWith<InvalidConfigurationError>
        {
            configuration.addTask( StubTaskDescriptor( "Duplicate name" ) )
        }
    }
}