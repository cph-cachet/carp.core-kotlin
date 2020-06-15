package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.protocols.infrastructure.test.StubTrigger
import kotlin.test.*


class UnusedDevicesWarningTest
{
    @Test
    fun isIssuePresent_true_with_unused_master_device()
    {
        val protocol = createEmptyProtocol()
        val unusedDevice = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( unusedDevice )

        val warning = UnusedDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val expectedUnused = listOf( unusedDevice )
        val unused = warning.getUnusedDevices( protocol )
        assertEquals( expectedUnused.count(), unused.intersect( expectedUnused ).count() )
    }

    @Test
    fun isIssuePresent_true_with_unused_connected_device()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor()
        val unusedConnected = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( master )
            addConnectedDevice( unusedConnected, master )
            addTriggeredTask( StubTrigger( master ), StubTaskDescriptor(), master )
        }

        val warning = UnusedDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val expectedUnused = listOf( unusedConnected )
        val unused = warning.getUnusedDevices( protocol )
        assertEquals( expectedUnused.count(), unused.intersect( expectedUnused ).count() )
    }

    @Test
    fun isIssuePresent_false_when_master_device_not_used_in_triggers_but_relays_data()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor()
        val connected = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( master )
            addConnectedDevice( connected, master )
            addTriggeredTask( StubTrigger( connected ), StubTaskDescriptor(), connected )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }

    @Test
    fun isIssuePresent_false_when_chained_master_devices_are_not_used_in_triggers_but_relay_data()
    {
        val protocol = createEmptyProtocol()
        val master1 = StubMasterDeviceDescriptor( "Master 1" )
        val master2 = StubMasterDeviceDescriptor( "Master 2" )
        val connected = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( master1 )
            addConnectedDevice( master2, master1 )
            addConnectedDevice( connected, master2 )
            addTriggeredTask( StubTrigger( connected ), StubTaskDescriptor(), connected )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }

    @Test
    fun isIssuePresent_false_when_all_devices_are_used_in_triggers()
    {
        val protocol = createEmptyProtocol()
        val device1 = StubMasterDeviceDescriptor()
        val device2 = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( device1 )
            addConnectedDevice( device2, device1 )
            addTriggeredTask( StubTrigger( device1 ), StubTaskDescriptor(), device2 )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }
}
