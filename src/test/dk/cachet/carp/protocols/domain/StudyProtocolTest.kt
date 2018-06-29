package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.deployment.*
import dk.cachet.carp.protocols.domain.devices.*
import dk.cachet.carp.protocols.domain.tasks.*
import dk.cachet.carp.protocols.domain.triggers.*
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.*
import org.junit.Assert.*


/**
 * Tests for [StudyProtocol].
 */
class StudyProtocolTest
{
    @Nested
    inner class Devices : DeviceConfigurationTest
    {
        override fun createDeviceConfiguration(): DeviceConfiguration
        {
            return createEmptyProtocol()
        }
    }

    @Nested
    inner class Tasks : TaskConfigurationTest
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
    fun `can't addTrigger which requires a master device for a normal device`()
    {
        val protocol = createEmptyProtocol()
        val masterDevice = StubMasterDeviceDescriptor()
        val connectedDevice = StubDeviceDescriptor()
        protocol.addMasterDevice( masterDevice )
        protocol.addConnectedDevice( connectedDevice, masterDevice )
        val trigger = StartOfStudyTrigger( connectedDevice.roleName )

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
        protocol.addMasterDevice( device )
        protocol.addTriggeredTask( trigger, task, device )

        val isAdded = protocol.addTriggeredTask( trigger, task, device )
        assertFalse( isAdded )
        assertEquals( 1, protocol.getTriggeredTasks( trigger ).count() )
    }

    @Test
    fun `addTriggeredTask adds triggers which are not yet included in the protocol`()
    {
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val task = StubTaskDescriptor()
        protocol.addMasterDevice( device )
        protocol.addTask( task )

        val trigger = StubTrigger( device )
        protocol.addTriggeredTask( trigger, task, device )
        assertTrue( protocol.triggers.contains( trigger ) )
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
    fun `deployment warning when a trigger sends more than one task to a single device`()
    {
        // Create a study protocol with a trigger which triggers two tasks to a single device.
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val trigger = StubTrigger( device )
        with ( protocol ) {
            addMasterDevice( device )
            addTriggeredTask( trigger, StubTaskDescriptor( "Task 1" ), device )
            addTriggeredTask( trigger, StubTaskDescriptor( "Task 2" ), device )
        }

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UseCompositeTaskWarning>().count() )
    }

    @Test
    fun `deployment warning when a device is never used in a trigger or never relays data from connected devices`()
    {
        // Create a study protocol with a device which is never used.
        val protocol = createEmptyProtocol()
        val device = StubMasterDeviceDescriptor()
        val unusedDevice = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( device )
            addConnectedDevice( unusedDevice, device )
            addTriggeredTask( StubTrigger( device ), StubTaskDescriptor(), device )
        }

        // Therefore, a warning is issued.
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UnusedDevicesWarning>().count() )
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
            addTriggeredTask( trigger1, task, device )
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
        assertEquals( 1, protocol.getDeploymentIssues().filterIsInstance<UntriggeredTasksWarning>().count() )
    }

    @Test
    fun `creating protocol fromSnapshot obtained by getSnapshot is the same`()
    {
        val protocol = createComplexProtocol()

        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        val fromSnapshot = StudyProtocol.fromSnapshot( snapshot )

        assertEquals( protocol.owner, fromSnapshot.owner )
        assertEquals( protocol.name, fromSnapshot.name )
        assertEquals( protocol.devices.count(), protocol.devices.intersect( fromSnapshot.devices ).count() )
        protocol.masterDevices.forEach { assertTrue( connectedDevicesAreSame( protocol, fromSnapshot, it ) ) }
        assertEquals( protocol.triggers.count(), protocol.triggers.intersect( fromSnapshot.triggers ).count() )
        assertEquals( protocol.tasks.count(), protocol.tasks.intersect( fromSnapshot.tasks ).count() )
        protocol.triggers.forEach {
            val triggeredTasks = protocol.getTriggeredTasks( it )
            val fromSnapshotTriggeredTasks = fromSnapshot.getTriggeredTasks( it )
            assertEquals( triggeredTasks.count(), triggeredTasks.intersect( fromSnapshotTriggeredTasks ).count() )
        }
    }

    @Test
    fun `create protocol fromSnapshot with custom extending types succeeds`()
    {
        val serialized = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot = StudyProtocolSnapshot.fromJson( serialized )

        StudyProtocol.fromSnapshot( snapshot )
    }

    private fun connectedDevicesAreSame( protocol: StudyProtocol, fromSnapshot: StudyProtocol, masterDevice: MasterDeviceDescriptor ): Boolean
    {
        val protocolConnected = protocol.getConnectedDevices( masterDevice ).sortedWith( compareBy { it.roleName } )
        val snapshotConnected = fromSnapshot.getConnectedDevices( masterDevice ).sortedWith( compareBy { it.roleName } )

        val areSameDevices = snapshotConnected.count() == protocolConnected.intersect( snapshotConnected ).count()
        return areSameDevices && protocolConnected.filterIsInstance<MasterDeviceDescriptor>().all { connectedDevicesAreSame( protocol, fromSnapshot, it ) }
    }
}