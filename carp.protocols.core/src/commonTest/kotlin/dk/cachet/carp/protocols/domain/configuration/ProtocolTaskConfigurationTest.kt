package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import kotlin.test.*


/**
 * Base class with tests for [ProtocolTaskConfiguration] which can be used to test extending types.
 */
interface ProtocolTaskConfigurationTest
{
    /**
     * Called for each test to create a task configuration to run tests on.
     */
    fun createTaskConfiguration(): ProtocolTaskConfiguration


    @Test
    fun addTask_succeeds()
    {
        val configuration = createTaskConfiguration()

        val task = StubTaskConfiguration()
        val isAdded: Boolean = configuration.addTask( task )
        assertTrue( isAdded )
        assertTrue( configuration.tasks.contains( task ) )
    }

    @Test
    fun removeTask_succeeds()
    {
        val configuration = createTaskConfiguration()
        val task = StubTaskConfiguration()
        configuration.addTask( task )

        val isRemoved: Boolean = configuration.removeTask( task )
        assertTrue( isRemoved )
        assertFalse( configuration.tasks.contains( task ) )
    }

    @Test
    fun removeTask_returns_false_when_task_not_present()
    {
        val configuration = createTaskConfiguration()
        val task = StubTaskConfiguration()

        val isRemoved: Boolean = configuration.removeTask( task )
        assertFalse( isRemoved )
    }

    @Test
    fun addTask_multiple_times_only_adds_first_time()
    {
        val configuration = createTaskConfiguration()
        val task = StubTaskConfiguration()
        configuration.addTask( task )

        val isAdded: Boolean = configuration.addTask( task )
        assertFalse( isAdded )
        assertEquals( 1, configuration.tasks.count() )
    }

    @Test
    fun do_not_allow_duplicate_names_for_tasks()
    {
        val configuration = createTaskConfiguration()
        configuration.addTask( StubTaskConfiguration( "Unique name" ) )
        configuration.addTask( StubTaskConfiguration( "Duplicate name" ) )

        // Adding an additional task with duplicate name should fail.
        assertFailsWith<IllegalArgumentException>
        {
            configuration.addTask( StubTaskConfiguration( "Duplicate name" ) )
        }
    }
}
