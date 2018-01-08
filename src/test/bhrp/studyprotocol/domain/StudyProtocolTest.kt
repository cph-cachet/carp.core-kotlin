package bhrp.studyprotocol.domain

import bhrp.studyprotocol.domain.deployment.*
import bhrp.studyprotocol.domain.devices.*
import bhrp.studyprotocol.domain.tasks.*
import bhrp.studyprotocol.domain.triggers.*
import org.junit.jupiter.api.*
import kotlin.test.*


/**
 * Tests for [StudyProtocol].
 */
class StudyProtocolTest
{
    class Devices : DeviceConfigurationTest
    {
        override fun createDeviceConfiguration(): DeviceConfiguration
        {
            return createEmptyProtocol()
        }
    }

    class Tasks : TaskConfigurationTest
    {
        override fun createTaskConfiguration(): TaskConfiguration
        {
            return createEmptyProtocol()
        }
    }


    @Test
    fun `one master device needed for deployment`()
    {
        // By default, no master device is defined in a study protocol.
        val protocol: StudyProtocol = createEmptyProtocol()

        // Therefore, the protocol is not deployable, indicated by an error in deployment issues.
        assertFalse( protocol.isDeployable() )
        assertEquals( 1, protocol.getDeploymentIssues().filter { it is NoMasterDeviceError }.count() )
    }


    @Test
    fun `addTrigger succeeds`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        val trigger = StubTrigger( device )

        val isAdded: Boolean = protocol.addTrigger( trigger )
        assertTrue( isAdded )
        assertTrue( protocol.triggers.contains( trigger ) )
    }

    @Test
    fun `addTrigger multiple times only adds first time`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( device )
        val trigger = StubTrigger( device )
        protocol.addTrigger( trigger )

        val isAdded: Boolean = protocol.addTrigger( trigger )
        assertFalse( isAdded )
        assertEquals( 1, protocol.triggers.count() )
    }

    @Test
    fun `can't addTrigger for device not included in the protocol`()
    {
        val protocol = createEmptyProtocol()
        val trigger = StubTrigger( StubDeviceDescriptor() )

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.addTrigger( trigger )
        }
    }

    @Test
    fun `addTriggeredTask succeeds`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val task = StubTaskDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTrigger( trigger )

        val isAdded: Boolean = protocol.addTriggeredTask( trigger, task, device )
        assertTrue( isAdded )
        assertTrue( protocol.getTriggeredTasks( trigger ).contains( TriggeredTask( task, device ) ) )
    }

    @Test
    fun `addTriggeredTasks multiple times only adds first time`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val task = StubTaskDescriptor()
        with ( protocol )
        {
            addMasterDevice( device )
            addTrigger( trigger )
            addTriggeredTask( trigger, task, device )
        }

        val isAdded = protocol.addTriggeredTask( trigger, task, device )
        assertFalse( isAdded )
        assertEquals( 1, protocol.getTriggeredTasks( trigger ).count() )
    }

    @Test
    fun `can't addTriggeredTask for trigger not included in the protocol`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val task = StubTaskDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTask( task )

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.addTriggeredTask( StubTrigger( device ), task, device )
        }
    }

    @Test
    fun `addTriggeredTask adds tasks which are not yet included in the protocol`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        protocol.addMasterDevice( device )
        protocol.addTrigger( trigger )

        val task = StubTaskDescriptor()
        protocol.addTriggeredTask( trigger, task, device )
        assertTrue( protocol.tasks.contains( task ) )
    }

    @Test
    fun `can't addTriggeredTask for device not included in the protocol`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val task = StubTaskDescriptor()
        with ( protocol )
        {
            addMasterDevice( device )
            addTrigger( trigger )
            addTask( task )
        }

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.addTriggeredTask( trigger, task, StubDeviceDescriptor() )
        }
    }

    @Test
    fun `getTriggeredTasks succeeds`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        val otherTrigger = StubTrigger( device, "Different" )
        val task = StubTaskDescriptor( "Task one" )
        with ( protocol )
        {
            addMasterDevice( device )
            addTrigger( trigger )
            addTrigger( otherTrigger )
            addTriggeredTask( trigger, task, device )
            addTriggeredTask( otherTrigger, StubTaskDescriptor( "Task two" ), device )
        }

        val triggeredTasks: List<TriggeredTask> = protocol.getTriggeredTasks( trigger ).toList()
        assertEquals( 1, triggeredTasks.count() )
        assertTrue( triggeredTasks.contains( TriggeredTask( task, device ) ) )
    }

    @Test
    fun `can't getTriggeredTasks for non-existing trigger`()
    {
        val protocol = createEmptyProtocol()

        assertFailsWith<InvalidConfigurationError>
        {
            protocol.getTriggeredTasks( StubTrigger( StubDeviceDescriptor() ) )
        }
    }

    @Test
    fun `triggers should not send more than one task to a single device`()
    {
        fail( "Triggers are not implemented yet." )
    }

    @Test
    fun `deployment warning when some devices never receive tasks`()
    {
        fail( "To implement" )
    }

    @Test
    fun `removeTask also removes it from triggers`()
    {
        // Create a study protocol with a task which is initiated by a trigger.
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val task = StubTaskDescriptor()
        val trigger1 = StubTrigger( device, "Trigger one" )
        val trigger2 = StubTrigger( device, "Trigger two" )
        with ( protocol )
        {
            addMasterDevice( device )
            addTrigger( trigger1 )
            addTriggeredTask( trigger1, task, device )
            addTrigger( trigger2 )
            addTriggeredTask( trigger2, task, device )
        }

        protocol.removeTask( task )
        assertEquals( 0, protocol.getTriggeredTasks( trigger1 ).count() )
        assertEquals( 0, protocol.getTriggeredTasks( trigger2 ).count() )
    }

    @Test
    fun `deployment warning when some tasks are never triggered`()
    {
        // Create a study protocol with a task which is never triggered.
        val protocol = createEmptyProtocol()
        protocol.addTask( StubTaskDescriptor() )

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filter { it is UntriggeredTasksWarning }.count() )
    }
}